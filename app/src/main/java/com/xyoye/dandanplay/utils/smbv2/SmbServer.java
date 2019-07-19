package com.xyoye.dandanplay.utils.smbv2;

import android.text.TextUtils;

import com.xyoye.dandanplay.utils.smbv2.http.HttpContentListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jcifs.smb.SmbFile;

/**
 * Created by xyoye on 2019/7/18.
 */

public class SmbServer extends Thread implements HttpContentListener {
    //smb绑定的本地端口
    public static int SMB_PORT = 2222;
    //smb绑定的本地IP
    public static String SMB_IP = "";

    //smb文件，用于获取视频内容及信息
    private static SmbFile playSmbFile;

    //用于接收客户端（播放器）请求的Socket
    private ServerSocket serverSocket = null;
    //本地可用IP地址列表
    private List<InetAddress> inetAddressList;

    public SmbServer() {
        getInetAddressList();
    }

    public static void setPlaySmbFile(SmbFile smbFile) {
        playSmbFile = smbFile;
    }

    public void stopSmbServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
                SMB_IP = "";
                SMB_PORT = 2222;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        super.run();

        //创建ServerSocket
        int retryCount = 0;
        int port = 2222;
        while (!createServerSocket(port)) {
            retryCount++;
            if (retryCount > 100) {
                return;
            }
            port++;
        }

        //在ServerSocket关闭之前一直监听请求
        while (!serverSocket.isClosed()){
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(getTimeOut());
                //接收到请求后，新建线程处理请求
                new SmbServerThread(socket, this).start();
            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
    }

    private synchronized int getTimeOut(){
        return 15 * 1000;
    }

    //获取本机接口地址
    private void getInetAddressList(){
        inetAddressList = new ArrayList<>();
        try {
            //机器上所有的接口
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) enumeration.nextElement();
                //绑定到此网络接口的InetAddress
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        inetAddressList.add(inetAddress);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    //创建ServerSocket
    private boolean createServerSocket(int port) {
        if (serverSocket != null) {
            return true;
        }
        for (int i = 0; i < inetAddressList.size(); i++) {
            String hostAddress = inetAddressList.get(i).getHostAddress();
            if (!TextUtils.isEmpty(hostAddress)) {
                try {
                    InetAddress inetAddress = InetAddress.getByName(hostAddress);
                    SMB_IP = hostAddress;
                    SMB_PORT = port;
                    serverSocket = new ServerSocket(SMB_PORT, 0, inetAddress);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    //获取视频内容
    public InputStream getContentInputStream() {
        InputStream inputStream = null;
        try {
            inputStream = playSmbFile.getInputStream();
        }catch (IOException e){
            e.printStackTrace();
        }
        return inputStream;
}

    @Override
    //获取视频格式
    public String getContentType() {
        String smbFilePath = playSmbFile.getPath();
        if (TextUtils.isEmpty(smbFilePath))
            return "";
        int lastPoi = smbFilePath.lastIndexOf('.');
        int lastSep = smbFilePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return "." +smbFilePath.substring(lastPoi + 1);
    }

    @Override
    //获取视频长度
    public long getContentLength() {
        return playSmbFile.getContentLengthLong();
    }
}
