package com.xyoye.dandanplay.torrent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.torrent.exception.TaskAlreadyAddedException;
import com.xyoye.dandanplay.torrent.info.ProxyInfo;
import com.xyoye.dandanplay.torrent.info.TaskStateBean;
import com.xyoye.dandanplay.torrent.info.Torrent;
import com.xyoye.dandanplay.torrent.utils.EngineSettings;
import com.xyoye.dandanplay.torrent.utils.StateParcelCache;
import com.xyoye.dandanplay.torrent.utils.TorrentConfig;
import com.xyoye.dandanplay.torrent.utils.TorrentEngineCallback;
import com.xyoye.dandanplay.torrent.utils.TorrentHelper;
import com.xyoye.dandanplay.torrent.utils.TorrentUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
    private NotificationManager notificationManager;
    private TorrentEngineCallback engineCallback;
    private Notification.Builder foregroundNotify;
    private Runnable syncNotifyRunnable;
    private Handler syncNotifyHandler;
    private Thread shutdownThread;

    private boolean isNetworkOnline;
    private boolean isAlreadyRunning;
    private AtomicBoolean needsUpdateNotify = new AtomicBoolean(false);
    private AtomicBoolean pauseTask = new AtomicBoolean(false);
    private StateParcelCache<TaskStateBean> taskStateCache = new StateParcelCache<>();

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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

        initListener();

        initHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isAlreadyRunning) {
            isAlreadyRunning = true;
            initTorrentEngine();
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
                        TorrentHelper.addTorrent(torrent);
                    } catch (Throwable e) {
                        handleAddTaskError(e);
                    }
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    /**
     * 初始化下载引擎
     */
    public void initTorrentEngine() {
        TorrentEngine.getInstance().setEngineCallback(engineCallback);
        TorrentEngine.getInstance().setSettings(new EngineSettings());
        TorrentEngine.getInstance().setProxy(getProxySettings());
        TorrentEngine.getInstance().start();
    }

    /**
     * 初始化下载回调
     */
    public void initListener() {
        engineCallback = new TorrentEngineCallback() {

            @Override
            public void onEngineStarted() {
                // TODO: 2019/8/22 恢复任务
                // TODO: 2019/8/22 开启流媒体服务
            }

            @Override
            public void onTorrentAdded(String id) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTask(id);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                //是否需要暂停任务
                if (pauseTask.get())
                    torrentTask.pause();

                //将下载中任务保存到数据库
                DataBaseManager.getInstance()
                        .selectTable(16)
                        .insert()
                        .param(1, torrent.getTaskName())
                        .param(2, torrent.getSaveDirPath())
                        .param(3, torrent.getTorrentFilePath())
                        .param(4, torrent.getTorrentHash())
                        .param(5, torrent.getAnimeTitle())
                        .param(6, torrent.getPrioritiesStr())
                        .execute();
            }

            @Override
            public void onTorrentStateChanged(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                TaskStateBean taskState = TorrentHelper.buildTaskState(torrentTask);
                if (taskStateCache.contains(taskState))
                    return;
                taskStateCache.put(taskState);
                // TODO: 2019/8/23 传递taskState，通知UI更新
            }

            @Override
            public void onTorrentFinished(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                //将已完成任务保存到数据库
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YY-MM-DD HH-mm-SS", Locale.getDefault());
                DataBaseManager.getInstance()
                        .selectTable(14)
                        .insert()
                        .param(1, torrent.getTaskName())
                        .param(2, torrent.getSaveDirPath())
                        .param(3, torrent.getTorrentFilePath())
                        .param(4, torrent.getTorrentHash())
                        .param(5, torrent.getAnimeTitle())
                        .param(6, torrentTask.getTotalWanted())
                        .param(7, simpleDateFormat.format(new Date()))
                        .execute();

                //从数据库中下载中任务删除
                DataBaseManager.getInstance()
                        .selectTable(16)
                        .delete()
                        .where(4, torrent.getTorrentHash())
                        .execute();

                // TODO: 2019/8/23 通知UI更新
            }


            @Override
            public void onTorrentRemoved(String hash) {
                if (taskStateCache.contains(hash))
                    taskStateCache.remove(hash);

                //从数据库中下载中任务删除
                DataBaseManager.getInstance()
                        .selectTable(16)
                        .delete()
                        .where(4, hash)
                        .execute();

                // TODO: 2019/8/23 通知UI更新
            }

            @Override
            public void onTorrentPaused(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTask(hash);
                if (torrentTask == null) return;
                Torrent torrent = torrentTask.getTorrent();
                if (torrent == null) return;

                torrentTask.getTorrent().setPaused(true);

                TaskStateBean taskState = TorrentHelper.buildTaskState(torrentTask);
                //没有状态一样的状态
                if (!taskStateCache.contains(taskState)) {
                    taskStateCache.put(taskState);
                    // TODO: 2019/8/23 传递taskState，通知UI更新
                }
            }

            @Override
            public void onTorrentResumed(String hash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTask(hash);
                if (torrentTask != null && !torrentTask.getTorrent().isPaused())
                    return;
                if (torrentTask != null) {
                    torrentTask.getTorrent().setPaused(false);
                }

                TaskStateBean taskState = TorrentHelper.buildTaskState(torrentTask);
                //没有状态一样的状态
                if (!taskStateCache.contains(taskState)) {
                    taskStateCache.put(taskState);
                    // TODO: 2019/8/23 传递taskState，通知UI更新
                }
            }

            @Override
            public void onTorrentMoved(String id, boolean success) {
                // TODO: 2019/8/22 更新通知
            }

            @Override
            public void onIpFilterParsed(boolean success) {
                Toast.makeText(
                        IApplication.get_context(),
                        success ? "解析IP过滤文件成功"
                                : "解析IP过滤文件失败，可能文件已损坏或格式不正确。",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRestoreSessionError(String hash) {
                if (hash == null)
                    return;
                Log.d(TAG, "Restore Session Failed");
            }

            @Override
            public void onTorrentError(String hash, String errorMsg) {
                if (errorMsg != null)
                    Log.e(TAG, "Torrent " + hash + ": " + errorMsg);

                TorrentTask task = TorrentEngine.getInstance().getTask(hash);
                Torrent torrent = task.getTorrent();
                torrent.setErrorMsg(errorMsg);
                task.setTorrent(torrent);
                task.pause();
            }

            @Override
            public void onSessionError(String errorMsg) {
                Log.e(TAG, "onSessionError: " + errorMsg);
            }

            @Override
            public void onNatError(String errorMsg) {
                Log.e(TAG, "NAT error: " + errorMsg);
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
                    boolean online = TorrentUtils.isConnectedNetwork();
                    if (isNetworkOnline != online) {
                        isNetworkOnline = online;
                        needsUpdateNotify.set(true);
                    }

                    if (needsUpdateNotify.get()) {
                        try {
                            needsUpdateNotify.set(false);
                            if (foregroundNotify != null) {
                                foregroundNotify.setContentText((isNetworkOnline ?
                                        "网络状态：在线" :
                                        "网络状态：离线"));
                                startForeground(NOTIFICATION_ID, foregroundNotify.build());
                            }

                        } catch (Exception ignore) {

                        }
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
        // TODO: 2019/8/23 关闭流媒体服务
        isAlreadyRunning = false;
        stopForeground(true);
        stopSelf();
    }

    /**
     * 处理添加任务失败
     */
    private void handleAddTaskError(Throwable e) {
        if (e instanceof TaskAlreadyAddedException) {
            return;
        }
        Log.e(TAG, Log.getStackTraceString(e));
        String message;
        if (e instanceof FileNotFoundException)
            message = "种子文件不存在";
        else if (e instanceof IOException)
            message = "创建下载任务失败：IO异常";
        else
            message = "创建下载任务失败";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取代理偏好
     */
    private ProxyInfo getProxySettings() {
        ProxyInfo proxy = new ProxyInfo();
        TorrentConfig.ProxyType type = TorrentConfig.getInstance().getProxyType();
        proxy.setProxyType(type);
        if (type == TorrentConfig.ProxyType.NONE)
            return proxy;

        proxy.setIP(TorrentConfig.getInstance().getProxyIp());
        proxy.setPort(TorrentConfig.getInstance().getProxyPort());
        proxy.setPeerEnable(TorrentConfig.getInstance().isProxyPeerEnable());
        if (TorrentConfig.getInstance().isProxyAuthEnable()) {
            proxy.setAccount(TorrentConfig.getInstance().getProxyAccount());
            proxy.setPassword(TorrentConfig.getInstance().getProxyPassword());
        }
        return proxy;
    }

    /**
     * 创建通知
     */
    private Notification buildNotification() {
        foregroundNotify = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("TorrentService")
                .setContentText("TorrentService running")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(null)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0})
                .setSound(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            foregroundNotify.setChannelId("com.xyoye.dandanplay.TorrentService.DownloadChannel");
        }
        Notification notify = foregroundNotify.build();
        notify.flags = Notification.FLAG_FOREGROUND_SERVICE;
        return notify;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class Action {
        public static final String ACTION_SHUTDOWN = "TorrentService.Action.ACTION_SHUTDOWN";
        public static final String ACTION_WIFI_ENABLED = "TorrentService.Action.ACTION_WIFI_ENABLED";
        public static final String ACTION_WIFI_DISABLED = "TorrentService.Action.ACTION_WIFI_DISABLED";
        public static final String ACTION_ADD_TORRENT = "TorrentService.Action.ACTION_ADD_TORRENT";
    }

    public static class IntentTag {
        public static final String ADD_TASK_TORRENT = "TorrentService.IntentTag.ADD_TASK_TORRENT";
    }
}
