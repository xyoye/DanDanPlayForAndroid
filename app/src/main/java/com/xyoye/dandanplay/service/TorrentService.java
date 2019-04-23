package com.xyoye.dandanplay.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.blankj.utilcode.util.LogUtils;
import com.github.axet.wget.SpeedInfo;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.ui.activities.DownloadMangerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.torrent.Torrent;
import com.xyoye.dandanplay.utils.torrent.TorrentEvent;
import com.xyoye.dandanplay.utils.torrent.TorrentStartEvent;
import com.xyoye.dandanplay.utils.torrent.TorrentStorage;
import com.xyoye.dandanplay.utils.torrent.TorrentTask;
import com.xyoye.dandanplay.utils.torrent.TorrentUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Iterator;

import libtorrent.BytesInfo;
import libtorrent.Libtorrent;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentService extends Service {
    private int NOTIFICATION_ID = 1;

    private TorrentTask torrentTask;
    private SpeedInfo downloaded = new SpeedInfo();
    private SpeedInfo uploaded = new SpeedInfo();
    private NotificationManager notificationManager;
    private Handler mHandler = IApplication.getMainHandler();
    private Runnable refresh;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartEvent(TorrentStartEvent event){
        Torrent torrent = event.getTorrent();
        if (torrent == null)
            return;
        if (!TorrentStorage.hashs.containsKey(torrent.getHash())){

            IApplication.torrentList.add(torrent);
            IApplication.torrentStorage.addHash(torrent.getHash(), torrent);

            TorrentUtil.saveTorrent(torrent);

            if (!torrentTask.start(torrent))
                torrent.setError(true);
            showNotification();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TorrentEvent event){
        if (event.getPosition() >= IApplication.torrentList.size()) {
            LogUtils.e("even position error");
            return;
        }
        Torrent torrent = IApplication.torrentList.get(event.getPosition());
        switch (event.getAction()){
            case TorrentEvent.EVENT_RESUME:
                if (!torrentTask.start(torrent))
                    torrent.setError(true);
                break;
            case TorrentEvent.EVENT_PAUSE:
                torrentTask.pause(event.getPosition());
                break;
            case TorrentEvent.EVENT_DELETE_TASK:
                torrentTask.pause(event.getPosition());
                TorrentUtil.deleteTorrent(torrent, false);
                Iterator<Torrent> iteratorTask = IApplication.torrentList.iterator();
                while (iteratorTask.hasNext()){
                    Torrent t = iteratorTask.next();
                    if (t.getPath().endsWith(torrent.getPath())){
                        iteratorTask.remove();
                        IApplication.torrentStorage.removeHash(t.getHash());
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                    }
                }
                break;
            case TorrentEvent.EVENT_DELETE_FILE:
                torrentTask.pause(event.getPosition());
                TorrentUtil.deleteTorrent(torrent, true);
                Iterator<Torrent> iteratorFile = IApplication.torrentList.iterator();
                while (iteratorFile.hasNext()){
                    Torrent t = iteratorFile.next();
                    if (t.getPath().endsWith(torrent.getPath())){
                        iteratorFile.remove();
                        IApplication.torrentStorage.removeHash(t.getHash());
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                    }
                }
                break;
            case TorrentEvent.EVENT_ALL_PAUSE:
                for (Torrent t : IApplication.torrentList){
                    torrentTask.pause(t);
                }
                break;
            case TorrentEvent.EVENT_ALL_START:
                for (int i=IApplication.torrentList.size()-1; i>=0; i--){
                    Torrent t = IApplication.torrentList.get(i);
                    if (!Libtorrent.torrentActive(t.getId())){
                        if (!torrentTask.start(t))
                            t.setError(true);
                    }
                }
                break;
            case TorrentEvent.EVENT_ALL_DELETE_TASK:
                Iterator<Torrent> iterator = IApplication.torrentList.iterator();
                while (iterator.hasNext()){
                    Torrent t = iterator.next();
                    torrentTask.pause(t);
                    TorrentUtil.deleteTorrent(t, false);
                    IApplication.torrentStorage.removeHash(t.getHash());
                    iterator.remove();
                }
                EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                break;
            case TorrentEvent.EVENT_ALL_DELETE_FILE:
                Iterator<Torrent> iterator2 = IApplication.torrentList.iterator();
                while (iterator2.hasNext()){
                    Torrent t = iterator2.next();
                    torrentTask.pause(t);
                    TorrentUtil.deleteTorrent(t, true);
                    IApplication.torrentStorage.removeHash(t.getHash());
                    iterator2.remove();
                }
                EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                break;

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("TorrentService onCreate");
        EventBus.getDefault().register(TorrentService.this);

        torrentTask = new TorrentTask(this.getApplicationContext());

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("com.xyoye.dandanplay.torrentservice.downloadchannel", "下载任务", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.enableLights(false);
            channel.setSound(null, null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        startForeground(NOTIFICATION_ID, showNotification());

        downloaded.start(0);
        uploaded.start(0);

        refresh = () -> {
            BytesInfo bytesInfo = Libtorrent.stats();
            downloaded.step(bytesInfo.getDownloaded());
            uploaded.step(bytesInfo.getUploaded());
            for (Torrent torrent : IApplication.torrentList){
                if (Libtorrent.torrentActive(torrent.getId())) {
                    // TODO: 2019/4/12 这里每秒都会更新一次数据库，而且还是在主线程，得优化 
                    TorrentUtil.updateTorrent(torrent);
                }
            }
            notificationManager.notify(NOTIFICATION_ID, showNotification());
            mHandler.postDelayed(refresh,1000);
        };
        refresh.run();
    }

    private Notification showNotification(){
        int doneTask = 0;
        int queueTask = 0;
        int pauseTask = 0;
        int doingTask = 0;
        for (Torrent torrent : IApplication.torrentList){
            if (torrent.getStatus() == -1)
                continue;
            if (torrent.isDone()){
                doneTask++;
                continue;
            }
            switch (Libtorrent.torrentStatus(torrent.getId())){
                case Libtorrent.StatusQueued:
                    queueTask++;
                    break;
                case Libtorrent.StatusPaused:
                    pauseTask++;
                    break;
                case Libtorrent.StatusDownloading:
                case Libtorrent.StatusSeeding:
                case Libtorrent.StatusChecking:
                    doingTask++;
                    break;
            }
        }
        String downloadStatus;
        if (IApplication.torrentList.size() == 0){
            downloadStatus = "无任务";
        }else if (doneTask == IApplication.torrentList.size()){
            downloadStatus = "已完成";
        }else if (doingTask > 0){
            downloadStatus = "下载中";
        }else if (queueTask > 0){
            downloadStatus = "连接中";
        }else if (pauseTask == IApplication.torrentList.size()){
            downloadStatus = "全部暂停";
        }else {
            downloadStatus = "未知";
        }

        String downloadSpeed = CommonUtils.convertFileSize(downloaded.getCurrentSpeed());
        String uploadSpeed = CommonUtils.convertFileSize(uploaded.getCurrentSpeed());

        return buildNotification(downloadStatus, doneTask, downloadSpeed, uploadSpeed);
    }

    private Notification buildNotification(String downloadStatus, int doneTask, String downLoadSpeed, String uploadSpeed) {

        String msg = downloadStatus;
        msg += " 进度："+doneTask+"/"+IApplication.torrentList.size();
        msg += " · ↓"+downLoadSpeed+"/s";
        msg += " · ↑"+uploadSpeed+"/s";

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, DownloadMangerActivity.class),
                0);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("弹弹play")
                .setContentText(msg)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0})
                .setSound(null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("com.xyoye.dandanplay.torrentservice.downloadchannel");
        }
        Notification notify = builder.build();
        notify.flags = Notification.FLAG_FOREGROUND_SERVICE ;

        return notify;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i("TorrentService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i("TorrentService onDestroy");
        mHandler.removeCallbacks(refresh);
        notificationManager.cancel(NOTIFICATION_ID);
        EventBus.getDefault().unregister(TorrentService.this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.i("TorrentService onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.i("TorrentService onUnbind");
        return super.onUnbind(intent);
    }
}
