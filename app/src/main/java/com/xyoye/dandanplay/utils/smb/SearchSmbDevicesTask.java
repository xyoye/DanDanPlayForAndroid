package com.xyoye.dandanplay.utils.smb;

import android.os.SystemClock;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.utils.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

import jcifs.Address;
import jcifs.context.SingletonContext;

/**
 * Created by xyoye on 2018/11/19.
 */

public class SearchSmbDevicesTask implements Runnable {
    private enum CALL{
        START,

        PROGRESS,

        ERROR,

        COMPLETE
    }

    private final static String TAG = SearchSmbDevicesTask.class.getSimpleName();

    private FindLanDevicesListener mListener;
    private LifecycleOwner lifecycleOwner;

    public SearchSmbDevicesTask(FindLanDevicesListener listener, LifecycleOwner lifecycleOwner) {
        mListener = listener;
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public void run() {
        String mLocalIp = new LocalIPUtil().getLocalIp();
        if (mLocalIp == null || mLocalIp.isEmpty()){
            onCallback(CALL.ERROR, 0, null, "获取手机IP地址失败");
            return;
        }
        String netRange = mLocalIp.substring(0, mLocalIp.lastIndexOf(".") + 1);

        LinkedList<SocketChannel> sockets = new LinkedList<>();

        Selector selector;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            onCallback(CALL.ERROR, 0, null, "启动IP遍历失败");
            return;
        }

        onCallback(CALL.START, 0, null, null);
        for (int i = 1; i < 255; i++) {
            final String ip = netRange.concat(String.valueOf(i));
            SocketChannel socketChannel = null;
            try {
                socketChannel = SocketChannel.open();
            } catch (IOException ignored) {
            }
            if (socketChannel == null) continue;
            sockets.add(socketChannel);

            try {
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
                socketChannel.connect(new InetSocketAddress(ip, 445));
            } catch (IOException ignored) {
            }
        }

        final long readStartTime = SystemClock.elapsedRealtime();
        while ((SystemClock.elapsedRealtime() - readStartTime) < 1000) {
            int readyChannels = 0;
            if (selector != null) {
                try {
                    readyChannels = selector.select(150);
                } catch (IOException ignored) {
                }
            }
            if (readyChannels == 0) continue;
            int totalChannel = selector.selectedKeys().size();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isValid() && key.isConnectable()) {
                    SocketChannel currentChannel = (SocketChannel) key.channel();

                    boolean v = false;
                    try {
                        v = currentChannel.finishConnect();
                    } catch (IOException ignored) {
                    } finally {
                        try {
                            currentChannel.close();
                        } catch (IOException ignored) {
                        }
                    }

                    if (v) {
                        int ipNumber = sockets.indexOf(currentChannel) + 1;
                        String ip = netRange.concat(String.valueOf(ipNumber));
                        String deviceName = "UnKnow";
                        try {
                            Address address = SingletonContext.getInstance().getNameServiceClient().getByName(ip);
                            address.firstCalledName();
                            deviceName = address.nextCalledName(SingletonContext.getInstance());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        SmbDeviceBean smbDeviceBean = new SmbDeviceBean();
                        smbDeviceBean.setUrl(ip);
                        smbDeviceBean.setName(deviceName);
                        smbDeviceBean.setSmbType(Constants.SmbSourceType.LAN_DEVICE);

                        int channelCount = selector.selectedKeys().size();
                        int progress = (int)((float)(totalChannel-channelCount) / (float)totalChannel * 100);
                        onCallback(CALL.PROGRESS, progress, smbDeviceBean, null);
                        Log.d(TAG, "found share at " + ip+", the device name is "+deviceName);
                    }
                }
            }
        }

        for (SocketChannel socketChannel : sockets) {
            try {
                socketChannel.close();
            } catch (IOException ignored) {
            }
        }
        try {
            if (selector != null)
                selector.close();
        } catch (IOException ignored) {
        }

        onCallback(CALL.COMPLETE, 0, null, null);
    }

    private void onCallback(CALL call, int progress, SmbDeviceBean smbDeviceBean, String msg){
        if (lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED)
            return;

        IApplication.getMainHandler().post(() -> {
            switch (call){
                case START:
                    mListener.onStart();
                    break;
                case PROGRESS:
                    mListener.onProgress(progress, smbDeviceBean);
                    break;
                case ERROR:
                    mListener.onError(msg);
                    break;
                case COMPLETE:
                    mListener.onComplete();
                    break;
            }
        });
    }

    public interface FindLanDevicesListener {
        void onStart();

        void onProgress(int progress, SmbDeviceBean deviceBean);

        void onError(String msg);

        void onComplete();
    }
}
