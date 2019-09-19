package com.xyoye.dandanplay.utils.jlibtorrent;

import android.database.Cursor;

import com.blankj.utilcode.util.LogUtils;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.ErrorCode;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.SessionErrorAlert;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.alerts.TorrentRemovedAlert;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xyoye on 2019/6/11.
 * <p>
 * 下载任务的管理器
 */

public class TorrentEngine extends SessionManager implements AlertListener, NewTaskRunnable.OnNewTaskCallBack {
    private static int[] ACCEPT_ALERT_TYPE = {
            AlertType.ADD_TORRENT.swig(),
            AlertType.TORRENT_REMOVED.swig(),
            AlertType.SESSION_ERROR.swig()
    };

    //下载引擎中的任务集合. key: hash, value: task
    private ConcurrentHashMap<String, TorrentTask> mTaskMap;
    //临时存储新增任务的Torrent数据
    private ConcurrentHashMap<String, Torrent> mNewTaskMap;
    //新增任务的集合
    private Queue<NewTaskRunnable> mNewTaskQueue;
    //新增任务的线程池
    private ExecutorService mNewTaskExecutor;
    //下载引擎与下载服务的回调
    private TorrentEngineCallback engineCallback;

    private TorrentEngine() {
        super(false);
        mTaskMap = new ConcurrentHashMap<>();
        mNewTaskMap = new ConcurrentHashMap<>();
        mNewTaskQueue = new LinkedList<>();
        mNewTaskExecutor = Executors.newCachedThreadPool();
        //添加回调
        addListener(this);
        //启动下载引擎
        start(new SessionParams(getEngineSetting()));
    }

    public static TorrentEngine getInstance() {
        return EngineHolder.engine;
    }

    @Override
    protected void onAfterStart() {
        //恢复未完成的任务
        restoreTask();
        if (engineCallback != null)
            engineCallback.onEngineStarted();
    }


    @Override
    protected void onBeforeStop() {
        for (TorrentTask task : mTaskMap.values()) {
            if (task == null) continue;
            task.saveResumeData(true);
        }
        if (swig() != null) {
            TorrentUtil.saveSessionData(saveState());
        }

        mTaskMap.clear();
        mNewTaskMap.clear();
        mNewTaskQueue.clear();
        removeListener(this);
    }

    /**
     * 添加新任务
     */
    public void newTask(Torrent torrent) {
        if (!torrent.isCanBeTask()) {
            throw new IllegalArgumentException("torrent params error");
        }

        torrent.setRestoreTask(false);
        mNewTaskQueue.add(new NewTaskRunnable(torrent, this));
        queueNewTask();
    }

    /**
     * 暂停所有任务下载
     */
    public void pauseAll() {
        for (TorrentTask torrentTask : mTaskMap.values()) {
            if (torrentTask == null)
                continue;
            torrentTask.pause();
        }
    }

    /**
     * 恢复所有任务下载
     */
    public void resumeAll() {
        for (TorrentTask torrentTask : mTaskMap.values()) {
            if (torrentTask == null)
                continue;
            torrentTask.resume();
        }
    }

    /**
     * 检查是否可退出下载服务
     */
    public boolean isAllowExit() {
        if (mTaskMap.values().size() == 0)
            return true;
        for (TorrentTask torrentTask : mTaskMap.values()) {
            if (!torrentTask.isPaused())
                return false;
        }
        return true;
    }

    /**
     * 获取任务列表
     */
    public Collection<TorrentTask> getTaskList() {
        return mTaskMap.values();
    }

    /**
     * 获取hash集合
     */
    public Enumeration<String> getHashList() {
        return mTaskMap.keys();
    }

    /**
     * 根据hash获取一个任务信息
     */
    public TorrentTask getTorrentTask(String hash) {
        return mTaskMap.get(hash);
    }

    /**
     * 根据hash移除一个任务信息
     */
    public void removeTorrentTask(String hash) {
        mTaskMap.remove(hash);
    }

    /**
     * 获取下载引擎回调
     */
    public TorrentEngineCallback getEngineCallback() {
        return engineCallback;
    }

    /**
     * 设置下载引擎回调
     */
    public void setEngineCallback(TorrentEngineCallback engineCallback) {
        this.engineCallback = engineCallback;
    }

    /**
     * 恢复未完成的下载任务
     */
    private void restoreTask() {
        DataBaseManager.getInstance()
                .selectTable("downloading_task")
                .query()
                .postExecute(new QueryAsyncResultCallback<List<Torrent>>() {
                    @Override
                    public List<Torrent> onQuery(Cursor cursor) {
                        List<Torrent> torrentList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            Torrent torrent = new Torrent(
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    cursor.getString(4)
                            );
                            torrentList.add(torrent);
                        }
                        return torrentList;
                    }

                    @Override
                    public void onResult(List<Torrent> result) {
                        for (Torrent torrent : result) {
                            if (!torrent.isCanBeTask())
                                continue;

                            torrent.setRestoreTask(true);
                            mNewTaskQueue.add(new NewTaskRunnable(torrent, TorrentEngine.this));
                        }
                        queueNewTask();
                    }
                });

    }

    /**
     * 执行新增任务队列中的任务
     */
    private void queueNewTask() {
        NewTaskRunnable newTaskRunnable = null;
        try {
            if (!mNewTaskQueue.isEmpty())
                newTaskRunnable = mNewTaskQueue.poll();
        } catch (Exception e) {
            return;
        }

        if (newTaskRunnable != null)
            mNewTaskExecutor.execute(newTaskRunnable);
    }

    /**
     * 获取下载总速度
     */
    public long getDownloadRate() {
        return stats().downloadRate();
    }

    /**
     * 获取上传总速度
     */
    public long getUploadRate() {
        return stats().uploadRate();
    }


    /**
     * 初始下载配置
     */
    private SettingsPack getEngineSetting() {
        return new SettingsPack()
                .setString(settings_pack.string_types.dht_bootstrap_nodes.swigValue(), TorrentUtil.getDhtBootstrapNodeString())//路由表
                .downloadRateLimit(TorrentConfig.getInstance().getMaxDownloadRate())//下载速度限制
                .uploadRateLimit(0)//上传速度限制
                .connectionsLimit(200)//连接数量限制
                .activeDhtLimit(88)//dht限制
                .anonymousMode(false)//是否为匿名模式
                .activeLimit(TorrentConfig.getInstance().getMaxTaskCount());//最大活动任务数量
    }

    /**
     * 外部更新配置
     */
    public void updateSetting() {
        applySettings(
                new SettingsPack()
                        .downloadRateLimit(TorrentConfig.getInstance().getMaxDownloadRate())
                        .activeLimit(TorrentConfig.getInstance().getMaxTaskCount()));
        if (swig() != null) {
            TorrentUtil.saveSessionData(saveState());
        }
    }

    @Override
    public int[] types() {
        return ACCEPT_ALERT_TYPE;
    }

    @Override
    public void alert(Alert<?> alert) {
        switch (alert.type()) {
            case ADD_TORRENT:
                TorrentAlert<?> torrentAlert = (TorrentAlert<?>) alert;
                TorrentHandle torrentHandle = find(torrentAlert.handle().infoHash());
                if (torrentHandle == null || !torrentHandle.isValid())
                    break;

                String hash = torrentHandle.infoHash().toHex();
                Torrent torrent = mNewTaskMap.remove(hash);
                if (torrent == null)
                    break;

                torrent.setTaskBuildTime(System.currentTimeMillis());
                mTaskMap.put(hash, new TorrentTask(torrent, torrentHandle, engineCallback));

                queueNewTask();

                if (engineCallback != null)
                    engineCallback.onTorrentAdded(hash, torrent.isRestoreTask());
                break;
            case TORRENT_REMOVED:
                mTaskMap.remove(((TorrentRemovedAlert) alert).infoHash().toHex());
                break;
            case SESSION_ERROR:

                SessionErrorAlert sessionErrorAlert = (SessionErrorAlert) alert;
                ErrorCode error = sessionErrorAlert.error();
                if (engineCallback != null)
                    engineCallback.onSessionError(TorrentUtil.getErrorMsg(error));
                break;
        }
    }

    @Override
    public boolean beforeAddTask(Torrent newTorrent) {
        //任务已存在不添加
        if (mTaskMap.containsKey(newTorrent.getHash()))
            return false;
        mNewTaskMap.put(newTorrent.getHash(), newTorrent);
        LogUtils.e("已执行任务添加");
        return true;
    }

    private static class EngineHolder {
        static TorrentEngine engine = new TorrentEngine();
    }


}
