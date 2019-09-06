package com.xyoye.dandanplay.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskStateBean;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentConfig;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEngine;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEngineCallback;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentTask;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentUtil;
import com.xyoye.dandanplay.utils.jlibtorrent.WifiReceiver;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xyoye on 2019/8/22.
 */

public class TorrentService extends Service {
    private static final String TAG = TorrentService.class.getSimpleName();

    //通知ID
    private static final int NOTIFICATION_ID = 1001;
    //任务刷新间隔时间
    private static final int NOTIFY_SYNC_TIME = 1000; //ms

    private WifiReceiver wifiReceiver;
    private TorrentEngineCallback engineCallback;
    private Runnable syncNotifyRunnable;
    private Handler syncNotifyHandler;
    private Thread shutdownThread;

    private boolean isAlreadyRunning;
    private AtomicBoolean pauseTask = new AtomicBoolean(false);
    public static ConcurrentHashMap<String, TaskStateBean> taskStateMap = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        initNotification();

        initData();

        initListener();

        initHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isAlreadyRunning) {
            isAlreadyRunning = true;
            TorrentEngine.getInstance().setEngineCallback(engineCallback);
        }

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Action.ACTION_SHUTDOWN:
                    if (shutdownThread != null && !shutdownThread.isAlive())
                        shutdownThread.start();
                    break;
                case Action.ACTION_ADD_TORRENT:
                    Torrent torrent = intent.getParcelableExtra(IntentTag.ADD_TASK_TORRENT);
                    try {
                        TorrentEngine.getInstance().newTask(torrent);
                    }catch (IllegalArgumentException e){
                        ToastUtils.showShort("添加下载任务失败，参数错误");
                    }
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //仅限wifi下载时，未连接wifi，暂停所有任务
        boolean isTaskPause = TorrentConfig.getInstance().isDownloadOnlyWifi() && !NetworkUtils.isWifiConnected();
        pauseTask.set(isTaskPause);

        wifiReceiver = new WifiReceiver(isConnected -> {
            if (TorrentConfig.getInstance().isDownloadOnlyWifi()) {
                if (isConnected) {
                    pauseTask.set(false);
                    TorrentEngine.getInstance().resumeAll();
                } else {
                    pauseTask.set(true);
                    TorrentEngine.getInstance().pauseAll();
                }
            }
        });

        shutdownThread = new Thread() {
            @Override
            public void run() {
                stopService();
            }
        };
    }

    /**
     * 初始化通知
     */
    private void initNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("com.xyoye.dandanplay.TorrentService.DownloadChannel", "TorrentService", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.enableLights(false);
            channel.setSound(null, null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    /**
     * 初始化下载回调
     */
    public void initListener() {
        engineCallback = new TorrentEngineCallback() {

            @Override
            public void onEngineStarted() {

            }

            @Override
            public void onTorrentAdded(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                //是否需要暂停任务
                if (pauseTask.get())
                    torrentTask.pause();

                //将下载中任务保存到数据库
                Cursor cursor = DataBaseManager.getInstance()
                        .selectTable(16)
                        .query()
                        .where(2, torrent.getHash())
                        .execute();

                if (cursor.getCount() == 0) {
                    TorrentUtil.insertNewTask(
                            torrent.getHash(),
                            torrent.getTorrentPath(),
                            torrent.getSaveDirPath(),
                            torrent.getAnimeTitle(),
                            torrent.getPriorityStr());
                }

                //通知UI更新
                updateUI(torrentTask);
            }

            @Override
            public void onTorrentStateChanged(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                //通知UI更新
                updateUI(torrentTask);
            }

            @Override
            public void onTorrentFinished(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                TorrentUtil.transferDownloaded(torrent, torrentTask);
                taskStateMap.remove(hash);
                TorrentEngine.getInstance().removeTorrentTask(hash);

                //通知UI更新
                updateUIDelete(torrent.getHash());
            }


            @Override
            public void onTorrentRemoved(String hash) {
                //从数据库中下载中任务删除
                TorrentUtil.deleteDownloadingData(hash);
                //从缓存中移除
                taskStateMap.remove(hash);

                // TODO: 2019/9/6 注意实际文件的删除

                //通知UI更新
                updateUIDelete(hash);
            }

            @Override
            public void onTorrentPaused(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                updateUI(torrentTask);
            }

            @Override
            public void onTorrentResumed(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                updateUI(torrentTask);
            }

            @Override
            public void onTorrentMoved(String hash, boolean success) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                updateUI(torrentTask);
            }

            @Override
            public void onTorrentError(String hash, String errorMsg) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                updateUI(torrentTask);
            }

            @Override
            public void onSessionError(String errorMsg) {
                ToastUtils.showShort("启动下载服务失败，请尝试重启应用");
                Log.e(TAG, "onSessionError: " + errorMsg);
            }
        };
    }

    /**
     * 初始化刷新Handler
     */
    private void initHandler() {
        syncNotifyHandler = new Handler();
        syncNotifyRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAlreadyRunning) {
                    if (!pauseTask.get()) {
                        for (TorrentTask torrentTask : TorrentEngine.getInstance().getTaskList()) {
                            IApplication.getMainHandler().post(() -> updateUI(torrentTask));
                        }
                        startForeground(NOTIFICATION_ID, buildNotification());
                    }
                }
                syncNotifyHandler.postDelayed(this, NOTIFY_SYNC_TIME);
            }
        };
        syncNotifyHandler.postDelayed(syncNotifyRunnable, NOTIFY_SYNC_TIME);
    }

    /**
     * 关闭服务
     */
    private void stopService() {
        try {
            unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException ignore) {
        }

        if (syncNotifyHandler != null) {
            syncNotifyHandler.removeCallbacks(syncNotifyRunnable);
        }

        TorrentEngine.getInstance().stop();
        isAlreadyRunning = false;
        stopForeground(true);
        stopSelf();
    }

    /**
     * 创建通知
     */
    private Notification buildNotification() {
        Notification.Builder foregroundNotify = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("DanDanPlay后台下载")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(null)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0})
                .setSound(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            foregroundNotify.setChannelId("com.xyoye.dandanplay.TorrentService.DownloadChannel");
        }

        if (isAlreadyRunning) {
            foregroundNotify.setContentText("任务数量: " + TorrentEngine.getInstance().getTaskList().size());
            String speed = "速度: " +
                    "↓" + CommonUtils.convertFileSize(TorrentEngine.getInstance().getDownloadRate()) + "/s ." +
                    "↑" + CommonUtils.convertFileSize(TorrentEngine.getInstance().getUploadRate()) + "/s";
            foregroundNotify.setContentText(speed);
        } else {
            foregroundNotify.setContentText("暂无任务");
        }
        Notification notify = foregroundNotify.build();
        notify.flags = Notification.FLAG_FOREGROUND_SERVICE;
        return notify;
    }

    /**
     * 发送EventBus通知UI更新
     */
    private void updateUI(TorrentTask torrentTask) {
        TaskStateBean taskState = TaskStateBean.buildTaskState(torrentTask);
        taskStateMap.remove(taskState.getTorrentHash());
        taskStateMap.put(taskState.getTorrentHash(), taskState);
    }

    /**
     * 发送EventBus通知UI删除Item
     */
    private void updateUIDelete(String hash) {
        EventBus.getDefault().post(hash);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class Action {
        public static final String ACTION_SHUTDOWN = "TorrentService.Action.ACTION_SHUTDOWN";
        public static final String ACTION_ADD_TORRENT = "TorrentService.Action.ACTION_ADD_TORRENT";
    }

    public static class IntentTag {
        public static final String ADD_TASK_TORRENT = "TorrentService.IntentTag.ADD_TASK_TORRENT";
    }
}
