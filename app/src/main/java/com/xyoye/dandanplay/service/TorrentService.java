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
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.bean.event.TorrentStartEvent;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.ui.activities.DownloadMangerActivity;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskStatus;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEvent;
import com.xyoye.dandanplay.utils.smb.TorrentServer;
import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPServerList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentService extends Service {
    private int NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;
    private Handler mHandler = IApplication.getMainHandler();
    private Runnable refresh;
    private TorrentServer torrentServer = null;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartEvent(TorrentStartEvent event){
        Torrent torrent = event.getTorrent();
        if (torrent == null)
            return;

        //根据hash判断任务是否已经存在
        if (!IApplication.taskMap.containsKey(torrent.getHash())){
            BtTask btTask = new BtTask(torrent);
            btTask.startTask();
            showNotification();

            if (torrentServer != null){
                HTTPServerList httpServerList = torrentServer.getHttpServerList();
                httpServerList.stop();
                httpServerList.close();
                httpServerList.clear();
                torrentServer.interrupt();
            }else {
                torrentServer = new TorrentServer(btTask);
                torrentServer.start();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TorrentEvent event){
        if (event.getPosition() >= IApplication.taskList.size()) {
            LogUtils.e("even position error");
            return;
        }
        //获取对应任务判断状态
        BtTask btTask = IApplication.taskList.get(event.getPosition());
        switch (event.getAction()){
            //唤醒任务
            case TorrentEvent.EVENT_RESUME:
                if (btTask.isPaused())
                    btTask.resume();
                break;
            //暂停任务
            case TorrentEvent.EVENT_PAUSE:
                if (btTask.isRunning() && !btTask.isPaused())
                    btTask.pause();
                break;
            //删除一个任务
            case TorrentEvent.EVENT_DELETE_TASK:
                btTask.pause();
                //TorrentUtil.deleteTorrent(torrent, false);
                IApplication.taskList.remove(event.getPosition());
                IApplication.taskMap.remove(btTask.getTorrent().getHash());
                DataBaseManager.getInstance()
                        .selectTable(6)
                        .delete()
                        .where(1, btTask.getTorrent().getTorrentPath())
                        .execute();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                break;
            //删除一个任务并且删除文件
            case TorrentEvent.EVENT_DELETE_FILE:
                btTask.pause();
                //TorrentUtil.deleteTorrent(torrent, false);
                IApplication.taskList.remove(event.getPosition());
                IApplication.taskMap.remove(btTask.getTorrent().getHash());
                DataBaseManager.getInstance()
                        .selectTable(6)
                        .delete()
                        .where(1, btTask.getTorrent().getTorrentPath())
                        .execute();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                break;
            //暂停全部任务
            case TorrentEvent.EVENT_ALL_PAUSE:
                for (BtTask task : IApplication.taskList){
                    if (task.isRunning() && !task.isPaused())
                        task.pause();
                }
                break;
            //开始所有任务
            case TorrentEvent.EVENT_ALL_START:
                for (BtTask task : IApplication.taskList){
                    if (task.isPaused())
                        task.resume();
                }
                break;
            //删除所有任务
            case TorrentEvent.EVENT_ALL_DELETE_TASK:
//                Iterator<Torrent> iterator = IApplication.torrentList.iterator();
//                while (iterator.hasNext()){
//                    Torrent t = iterator.next();
//                    torrentTask.pause(t);
//                    TorrentUtil.deleteTorrent(t, false);
//                    IApplication.torrentStorage.removeHash(t.getHash());
//                    iterator.remove();
//                }
//                EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                break;
            //删除所有任务并且删除文件
            case TorrentEvent.EVENT_ALL_DELETE_FILE:
//                Iterator<Torrent> iterator2 = IApplication.torrentList.iterator();
//                while (iterator2.hasNext()){
//                    Torrent t = iterator2.next();
//                    torrentTask.pause(t);
//                    TorrentUtil.deleteTorrent(t, true);
//                    IApplication.torrentStorage.removeHash(t.getHash());
//                    iterator2.remove();
//                }
//                EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOAD_MANAGER));
                break;
            case TorrentEvent.EVENT_CLOSE_PLAY:
                if (torrentServer == null)
                    return;
                HTTPServerList httpServerList = torrentServer.getHttpServerList();
                httpServerList.stop();
                httpServerList.close();
                httpServerList.clear();
                torrentServer.interrupt();
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("TorrentService onCreate");
        EventBus.getDefault().register(TorrentService.this);

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

        //刷新通知栏下载速度
        refresh = () -> {
//            if (IApplication.torrentList.size() > 0){
//                BytesInfo bytesInfo = Libtorrent.stats();
//                downloaded.step(bytesInfo.getDownloaded());
//                uploaded.step(bytesInfo.getUploaded());
//                notificationManager.notify(NOTIFICATION_ID, showNotification());
//            }
            mHandler.postDelayed(refresh,1000);
        };
        refresh.run();
    }

    private Notification showNotification(){
        int doneTask = 0;
        int queueTask = 0;
        int pauseTask = 0;
        int doingTask = 0;
        for (BtTask task : IApplication.taskList){
            TaskStatus taskStatus = task.getTaskStatus();
            switch (taskStatus){
                case FINISHED:
                    doneTask++;
                    break;
                case ALLOCATING:
                    queueTask++;
                    break;
                case PAUSED:
                    pauseTask++;
                    break;
                case DOWNLOADING:
                case SEEDING:
                case CHECKING:
                case DOWNLOADING_METADATA:
                    doingTask++;
                    break;
            }
        }
        String downloadStatus;
        if (IApplication.taskList.size() == 0){
            downloadStatus = "无任务";
        }else if (doneTask == IApplication.taskList.size()){
            downloadStatus = "已完成";
        }else if (doingTask > 0){
            downloadStatus = "下载中";
        }else if (queueTask > 0){
            downloadStatus = "连接中";
        }else if (pauseTask == IApplication.taskList.size()){
            downloadStatus = "全部暂停";
        }else {
            downloadStatus = "未知";
        }

        return buildNotification(downloadStatus, doneTask, "0B", "0B");
    }

    private Notification buildNotification(String downloadStatus, int doneTask, String downLoadSpeed, String uploadSpeed) {

        String msg = downloadStatus;
        msg += " 进度："+doneTask+"/"+IApplication.taskList.size();
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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(refresh);
        notificationManager.cancel(NOTIFICATION_ID);
        EventBus.getDefault().unregister(TorrentService.this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
