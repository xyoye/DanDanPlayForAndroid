package com.xyoye.dandanplay.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.TorrentServiceEvent;
import com.xyoye.dandanplay.utils.CommonUtils;
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
    private Runnable syncNotifyRunnable;
    private Handler syncNotifyHandler;

    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    public static ConcurrentHashMap<String, TaskStateBean> taskStateMap = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        initData();

        initListener();

        updateUIData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (Action.ACTION_ADD_TORRENT.equals(intent.getAction())) {
                Torrent torrent = intent.getParcelableExtra(IntentTag.ADD_TASK_TORRENT);
                try {
                    TorrentEngine.getInstance().newTask(torrent);
                } catch (IllegalArgumentException e) {
                    ToastUtils.showShort("添加下载任务失败，参数错误");
                }
            }
        }

        return START_NOT_STICKY;
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //初始化广播
        wifiReceiver = new WifiReceiver(isConnected -> {
            if (TorrentConfig.getInstance().isDownloadOnlyWifi() && !isConnected) {
                ToastUtils.showShort("wifi连接断开，已暂停所有下载任务");
                TorrentEngine.getInstance().pauseAll();
            }
        });
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        //初始化刷新
        syncNotifyHandler = new Handler();
        syncNotifyRunnable = new Runnable() {
            @Override
            public void run() {
                isRefreshing.set(true);
                updateUIData();
                startForeground(NOTIFICATION_ID, buildNotification());
                syncNotifyHandler.postDelayed(this, NOTIFY_SYNC_TIME);
            }
        };

        //初始化通知
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
        TorrentEngine.getInstance().setEngineCallback(new TorrentEngineCallback() {

            @Override
            public void onEngineStarted() {

            }

            @Override
            public void onTorrentAdded(String hash, boolean isRestore) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                //是否需要暂停任务(仅限wifi下载，恢复任务)
                boolean pauseTask = TorrentConfig.getInstance().isDownloadOnlyWifi() && !NetworkUtils.isWifiConnected();
                LogUtils.e("pauseTask: " + pauseTask + "   isRestore: " + isRestore);
                if (pauseTask || isRestore)
                    torrentTask.pause();
                else
                    torrentTask.resume();

                //将下载中任务保存到数据库
                TorrentUtil.insertNewTask(torrent);

                updateUI(torrentTask);
            }

            @Override
            public void onTorrentStateChanged(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                updateUI(torrentTask);
            }

            @Override
            public void onTorrentFinished(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                //移动数据库数据
                TorrentUtil.transferDownloaded(torrent, torrentTask);
                //移除任务
                TorrentEngine.getInstance().removeTorrentTask(hash);

                updateUI(torrent.getHash(), true);
            }


            @Override
            public void onTorrentRemoved(Torrent torrent) {
                //从数据库中下载中任务删除
                TorrentUtil.deleteDownloadingData(torrent.getHash());

                //删除实际文件
                TorrentUtil.deleteTaskFile(torrent.getSaveDirPath());

                updateUI(torrent.getHash(), false);
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

                if (!isRefreshing.get()) {
                    syncNotifyHandler.postDelayed(syncNotifyRunnable, NOTIFY_SYNC_TIME);
                }

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
        });
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

        foregroundNotify.setContentText("任务数量: " + TorrentEngine.getInstance().getTaskList().size());
        String speed = "速度: " +
                "↓" + CommonUtils.convertFileSize(TorrentEngine.getInstance().getDownloadRate()) + "/s ." +
                "↑" + CommonUtils.convertFileSize(TorrentEngine.getInstance().getUploadRate()) + "/s";
        foregroundNotify.setContentText(speed);
        Notification notify = foregroundNotify.build();
        notify.flags = Notification.FLAG_FOREGROUND_SERVICE;
        return notify;
    }

    /**
     * 通知UI更新
     */
    private void updateUI(TorrentTask torrentTask) {
        TaskStateBean taskState = TaskStateBean.buildTaskState(torrentTask);
        String hash = taskState.getTorrentHash();
        taskStateMap.remove(hash);
        taskStateMap.put(hash, taskState);
        EventBus.getDefault().post(new TorrentServiceEvent());
    }

    /**
     * 通知UI更新
     */
    private void updateUI(String hash, boolean isTaskFinish) {
        taskStateMap.remove(hash);
        EventBus.getDefault().post(new TorrentServiceEvent(isTaskFinish));
    }

    /**
     * 刷新界面任务数据
     */
    private void updateUIData() {
        for (TorrentTask torrentTask : TorrentEngine.getInstance().getTaskList()) {
            TaskStateBean taskState = TaskStateBean.buildTaskState(torrentTask);
            String hash = taskState.getTorrentHash();
            taskStateMap.remove(hash);
            taskStateMap.put(hash, taskState);
        }
        EventBus.getDefault().post(new TorrentServiceEvent());
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException ignore) {
        }

        if (syncNotifyHandler != null) {
            syncNotifyHandler.removeCallbacks(syncNotifyRunnable);
            isRefreshing.set(false);
        }

        taskStateMap.clear();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class Action {
        public static final String ACTION_ADD_TORRENT = "TorrentService.Action.ACTION_ADD_TORRENT";
    }

    public static class IntentTag {
        public static final String ADD_TASK_TORRENT = "TorrentService.IntentTag.ADD_TASK_TORRENT";
    }
}
