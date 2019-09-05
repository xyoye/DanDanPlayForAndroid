package com.xyoye.dandanplay.utils.jlibtorrent;

import android.text.TextUtils;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.AnnounceEntry;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.xyoye.dandanplay.app.IApplication;

import java.io.File;

import io.reactivex.annotations.Nullable;

/**
 * Created by xyoye on 2019/6/11.
 * 下载任务的管理器
 * 每一项任务拥有单独的管理器
 */

public class TorrentTask extends SessionManager implements AlertListener {
    private static int[] ACCEPT_ALERT_TYPE = {
            AlertType.ADD_TORRENT.swig(),
            AlertType.TORRENT_CHECKED.swig(),
            AlertType.TORRENT_FINISHED.swig(),
            AlertType.STATE_CHANGED.swig()
    };

    private Torrent mTorrent;
    private TorrentHandle mTorrentHandle;

    public TorrentTask(Torrent torrent) {
        // TODO: 2019/9/5 开发中开启log
        super(true);

        //检查输入参数
        if (TextUtils.isEmpty(torrent.getTorrentPath()) ||
                TextUtils.isEmpty(torrent.getSaveDirPath()) ||
                TextUtils.isEmpty(torrent.getHash()) ||
                torrent.getTorrentFileList().size() == 0) {
            throw new IllegalArgumentException("add torrent task params error");
        }

        this.mTorrent = torrent;
        this.addListener(this);

        SessionParams sessionParams = new SessionParams(
                new SettingsPack()
                        .setString(settings_pack.string_types.dht_bootstrap_nodes.swigValue(), TorrentUtil.getDhtBootstrapNodeString())//路由表
                        .downloadRateLimit(0)//下载速度限制
                        .uploadRateLimit(0)//上传速度限制
                        .connectionsLimit(200)//连接数量限制
                        .activeDhtLimit(88)//dht限制
                        .anonymousMode(false)//是否为匿名模式
        );

        start(sessionParams);
    }

    /**
     * 启动任务
     */
    public void startTask() {
        //下载的信息
        TorrentInfo torrentInfo = TorrentUtil.getTorrentInfoForFile(mTorrent.getTorrentPath());
        //保存文件夹
        File saveDirFile = new File(mTorrent.getSaveDirPath());
        //开始下载
        download(torrentInfo, saveDirFile, null, mTorrent.getPriporities(), null);
    }

    @Nullable
    public Torrent getTorrent() {
        return mTorrent;
    }

    @Override
    public int[] types() {
        return ACCEPT_ALERT_TYPE;
    }

    @Override
    public void alert(Alert<?> alert) {
        mTorrentHandle = ((TorrentAlert) alert).handle();
        switch (alert.type()) {
            case ADD_TORRENT:
                mTorrent.setTitle(mTorrentHandle.name());
                //添加tracker
                for (String trackerStr : IApplication.trackers) {
                    mTorrentHandle.addTracker(new AnnounceEntry(trackerStr));
                }
                //加入任务集合
                TaskInfo.taskList.add(this);
                TaskInfo.taskMap.put(mTorrent.getHash(), TaskInfo.taskList.size() - 1);
                //是否为恢复任务
                if (mTorrent.isRecoveryTask()) {
                    pause();
                } else {
                    TorrentUtil.insertNewTask(
                            mTorrent.getTorrentPath(),
                            mTorrent.getHash(),
                            mTorrent.getAnimeTitle(),
                            mTorrent.getPriorityStr()
                    );
                }
                break;
            case TORRENT_CHECKED:
                //是否为恢复任务
                if (mTorrent.isRecoveryTask()) {
                    pause();
                }
                break;
            case TORRENT_FINISHED:

                break;
        }
    }

    public TaskStatus getTaskStatus() {

        if (!mTorrentHandle.isValid()) {
            return TaskStatus.ERROR;
        }

        TorrentStatus status = mTorrentHandle.status();
        boolean isPaused = status.flags().and_(TorrentFlags.PAUSED).nonZero();

        if (isPaused && status.isFinished()) {
            return TaskStatus.FINISHED;
        }

        if (isPaused && !status.isFinished()) {
            return TaskStatus.PAUSED;
        }

        if (!isPaused && status.isFinished()) {
            return TaskStatus.SEEDING;
        }

        final TorrentStatus.State state = status.state();

        switch (state) {
            case CHECKING_FILES:
                return TaskStatus.CHECKING;
            case DOWNLOADING_METADATA:
                return TaskStatus.DOWNLOADING_METADATA;
            case DOWNLOADING:
                return TaskStatus.DOWNLOADING;
            case FINISHED:
                return TaskStatus.FINISHED;
            case SEEDING:
                return TaskStatus.SEEDING;
            case ALLOCATING:
                return TaskStatus.ALLOCATING;
            case CHECKING_RESUME_DATA:
                return TaskStatus.CHECKING;
            case UNKNOWN:
                return TaskStatus.UNKNOWN;
            default:
                return TaskStatus.UNKNOWN;
        }
    }
}
