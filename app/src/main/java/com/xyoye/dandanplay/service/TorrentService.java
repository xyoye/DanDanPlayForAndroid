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
import com.xyoye.dandanplay.ui.activities.personal.DownloadManagerActivityV2;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskStatus;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEvent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentUtil;
import com.xyoye.dandanplay.utils.smbv2.TorrentServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentService extends Service {
    private int NOTIFICATION_ID = 1001;

    private NotificationManager notificationManager;
    private Handler mHandler = IApplication.getMainHandler();
    private Runnable refresh;
    private TorrentServer torrentServer = null;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartEvent(TorrentStartEvent event){
        Torrent torrent = event.getTorrent();
        BtTask btTask = new BtTask(torrent);
        IApplication.taskList.add(btTask);
        IApplication.taskMap.put(torrent.getHash(), IApplication.taskList.size()-1);
        btTask.startTask(false);
        showNotification();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TorrentEvent event){
        if (event.getPosition() >= IApplication.taskList.size()) {
            LogUtils.e("even position error");
            return;
        }
        //获取对应任务判断状态
        switch (event.getAction()){
            //唤醒任务
            case TorrentEvent.EVENT_RESUME:
                BtTask resumeTask = IApplication.taskList.get(event.getPosition());
                if (resumeTask.isPaused()){
                    resumeTask.resume();
                }
                break;
            //暂停任务
            case TorrentEvent.EVENT_PAUSE:
                BtTask pauseTask = IApplication.taskList.get(event.getPosition());
                if (pauseTask.isRunning() && !pauseTask.isPaused()){
                    pauseTask.pause();
                }
                break;
            //删除一个任务
            case TorrentEvent.EVENT_DELETE_TASK:
                deleteTask(event.getPosition(), event.isDeleteFile());
                break;
            //暂停全部任务
            case TorrentEvent.EVENT_ALL_PAUSE:
                for (BtTask task : IApplication.taskList){
                    if (!task.isPaused()){
                        task.pause();
                    }
                }
                break;
            //开始所有任务
            case TorrentEvent.EVENT_ALL_START:
                for (BtTask task : IApplication.taskList){
                    if (task.isPaused()) {
                        task.resume();
                    }
                }
                break;
            //删除所有任务
            case TorrentEvent.EVENT_DELETE_ALL_TASK:
                for (int i=0; i<IApplication.taskList.size(); i++){
                    deleteTask(i, event.isDeleteFile());
                }
                break;
            case TorrentEvent.EVENT_PREPARE_PLAY:
                BtTask playTask = IApplication.taskList.get(event.getPosition());
                if (torrentServer != null){
                    torrentServer.setTorrentTask(playTask);
                }else {
                    torrentServer = new TorrentServer();
                    torrentServer.start();
                }
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
            if (IApplication.taskList.size() > 0){
                notificationManager.notify(NOTIFICATION_ID, showNotification());
            }
            mHandler.postDelayed(refresh,3000);
        };
        refresh.run();

        if (torrentServer == null){
            torrentServer = new TorrentServer();
            torrentServer.start();
        }
    }

    private Notification showNotification(){
        int doneTask = 0;
        int pauseTask = 0;
        int doingTask = 0;
        for (BtTask task : IApplication.taskList){
            TaskStatus taskStatus = task.getTaskStatus();
            switch (taskStatus){
                case FINISHED:
                    doneTask++;
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
        }else if (pauseTask == IApplication.taskList.size()){
            downloadStatus = "全部暂停";
        }else {
            downloadStatus = "未知";
        }

        return buildNotification(downloadStatus, doneTask);
    }

    private Notification buildNotification(String downloadStatus, int doneTask) {

        String msg = downloadStatus;
        msg += " 进度："+doneTask+"/"+IApplication.taskList.size();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, DownloadManagerActivityV2.class),
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
        if (torrentServer != null){
            torrentServer.stopTorrentServer();
        }

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

    private void deleteTask(int position, boolean isDeleteFile){
        BtTask btTask = IApplication.taskList.get(position);
        //暂停任务
        if (btTask.isRunning() && !btTask.isPaused()){
            btTask.pause();
        }
        //从内存中移除数据
        IApplication.taskList.remove(position);
        IApplication.taskMap.remove(btTask.getTorrent().getHash());
        //新建线程操作数据库及文件
        IApplication.getExecutor().execute(() -> {
            //从数据库中移除任务
            TorrentUtil.deleteDBTorrent(btTask.getTorrent().getTorrentPath());
            //删除文件
            if (isDeleteFile){
                TorrentUtil.deleteTaskFile(btTask.getTorrent());
            }
        });
        //通知activity刷新界面
        EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_DOWNLOADING_TASK));
    }
}
