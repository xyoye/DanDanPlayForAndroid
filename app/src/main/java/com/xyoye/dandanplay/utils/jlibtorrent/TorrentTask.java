package com.xyoye.dandanplay.utils.jlibtorrent;

import android.util.Log;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.AnnounceEntry;
import com.frostwire.jlibtorrent.ErrorCode;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.SessionHandle;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.Vectors;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.FileErrorAlert;
import com.frostwire.jlibtorrent.alerts.SaveResumeDataAlert;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.alerts.TorrentErrorAlert;
import com.frostwire.jlibtorrent.swig.add_torrent_params;
import com.frostwire.jlibtorrent.swig.byte_vector;
import com.xyoye.dandanplay.app.IApplication;

import java.util.List;

/**
 * Created by xyoye on 2019/9/6.
 */

public class TorrentTask {
    private static final String TAG = TorrentTask.class.getSimpleName();
    private static final int[] INNER_LISTENER_TYPES = new int[]{
            AlertType.BLOCK_FINISHED.swig(),
            AlertType.STATE_CHANGED.swig(),
            AlertType.TORRENT_FINISHED.swig(),
            AlertType.TORRENT_REMOVED.swig(),
            AlertType.TORRENT_PAUSED.swig(),
            AlertType.TORRENT_RESUMED.swig(),
            AlertType.STATS.swig(),
            AlertType.SAVE_RESUME_DATA.swig(),
            AlertType.STORAGE_MOVED.swig(),
            AlertType.STORAGE_MOVED_FAILED.swig(),
            AlertType.PIECE_FINISHED.swig(),
            AlertType.READ_PIECE.swig(),
            AlertType.TORRENT_ERROR.swig(),
            AlertType.FILE_ERROR.swig()
    };

    //每次保存数据需要间隔10秒
    private static final long SAVE_RESUME_SYNC_TIME = 5000;

    private Torrent mTorrent;
    private TaskListener taskListener;
    private TorrentHandle mTorrentHandle;
    private TorrentEngineCallback engineCallback;

    private long lastSaveResumeTime;


    public TorrentTask(Torrent mTorrent, TorrentHandle mTorrentHandle, TorrentEngineCallback engineCallback) {
        this.mTorrent = mTorrent;
        this.mTorrentHandle = mTorrentHandle;
        this.engineCallback = engineCallback;
        this.taskListener = new TaskListener();
        TorrentEngine.getInstance().addListener(taskListener);
        addTrackers(IApplication.trackers);
    }

    /**
     * 暂停
     */
    public void pause() {
        if (!mTorrentHandle.isValid())
            return;

        mTorrentHandle.unsetFlags(TorrentFlags.AUTO_MANAGED);
        mTorrentHandle.pause();
        saveResumeData(true);
    }

    /**
     * 继续
     */
    public void resume() {
        if (!mTorrentHandle.isValid())
            return;
        mTorrentHandle.setFlags(TorrentFlags.AUTO_MANAGED);
        mTorrentHandle.resume();
        saveResumeData(true);
    }

    /**
     * 设置下载速度上限
     */
    public void setDownloadSpeedLimit(int limit) {
        mTorrentHandle.setDownloadLimit(limit);
        saveResumeData(true);
    }

    /**
     * 设置上传速度上限
     */
    public void setUploadSpeedLimit(int limit) {
        mTorrentHandle.setUploadLimit(limit);
        saveResumeData(true);
    }

    /**
     * 移除任务
     */
    public void remove(boolean withFiles) {
        if (mTorrentHandle.isValid()) {
            if (withFiles)
                TorrentEngine.getInstance().remove(mTorrentHandle, SessionHandle.DELETE_FILES);
            else
                TorrentEngine.getInstance().remove(mTorrentHandle);
        }
    }

    /**
     * 增加tracker
     */
    public void addTrackers(List<String> trackers) {
        for (String url : trackers)
            mTorrentHandle.addTracker(new AnnounceEntry(url));
        saveResumeData(true);
    }

    /**
     * 获取下载种子
     */
    public Torrent getTorrent() {
        return mTorrent;
    }

    /**
     * 获取子文件的下载进度
     */
    public long[] getChildFileProgress() {
        return mTorrentHandle.fileProgress();
    }

    /**
     * 获取子文件信息
     */
    public FileStorage getTorrentFiles(){
        if (!mTorrentHandle.isValid())
            return null;

        return mTorrentHandle.torrentFile().files();
    }

    /**
     * 获取需要下载的总大小
     */
    public long getSize() {
        if (!mTorrentHandle.isValid())
            return 0;

        TorrentInfo info = mTorrentHandle.torrentFile();

        return info != null ? info.totalSize() : 0;
    }

    /**
     * 获取种子hash
     */
    public String getInfoHash() {
        return mTorrentHandle.infoHash().toString();
    }

    /**
     * 获取有效的已下载的大小
     */
    public long getReceivedBytes() {
        return mTorrentHandle.isValid() ? mTorrentHandle.status().totalPayloadDownload() : 0;
    }

    /**
     * 获取已下载的总下载大小
     */
    public long getTotalReceivedBytes() {
        return mTorrentHandle.isValid() ? mTorrentHandle.status().allTimeDownload() : 0;
    }


    /**
     * 获取下载进度
     */
    public int getProgress() {
        if (mTorrentHandle == null || !mTorrentHandle.isValid())
            return 0;

        if (mTorrentHandle.status() == null)
            return 0;

        float fp = mTorrentHandle.status().progress();
        TorrentStatus.State state = mTorrentHandle.status().state();
        if (Float.compare(fp, 1f) == 0 && state != TorrentStatus.State.CHECKING_FILES)
            return 100;

        int p = (int) (mTorrentHandle.status().progress() * 100);
        if (p > 0 && state != TorrentStatus.State.CHECKING_FILES) {
            return Math.min(p, 100);
        }

        final long received = getTotalReceivedBytes();
        final long size = getSize();
        if (size == received)
            return 100;
        if (size > 0) {
            p = (int) ((received * 100) / size);
            return Math.min(p, 100);
        }

        return 0;
    }


    /**
     * 获取子文件已下载大小
     */
    public long[] getFilesReceivedBytes() {
        if (!mTorrentHandle.isValid()) {
            return null;
        }

        return mTorrentHandle.fileProgress(TorrentHandle.FileProgressFlags.PIECE_GRANULARITY);
    }

    /**
     * 获取已上传的总大小
     */
    public long getTotalSentBytes() {
        return mTorrentHandle.isValid() ? mTorrentHandle.status().allTimeUpload() : 0;
    }

    /**
     * 获取希望下载的总大小
     */
    public long getTotalWanted() {
        return mTorrentHandle.isValid() ? mTorrentHandle.status().totalWanted() : 0;
    }

    /**
     * 获取下载速度
     */
    public long getDownloadSpeed() {
        return (!mTorrentHandle.isValid() || isFinished() || isPaused() || isSeeding()) ? 0 : mTorrentHandle.status().downloadPayloadRate();
    }


    /**
     * 获取上传速度
     */
    public long getUploadSpeed() {
        return (!mTorrentHandle.isValid() || (isFinished() && !isSeeding()) || isPaused()) ? 0 : mTorrentHandle.status().uploadPayloadRate();
    }

    /**
     * 获取种子状态
     */
    public TorrentStatus getTorrentStatus() {
        return mTorrentHandle.isValid() ? mTorrentHandle.status() : null;
    }

    /**
     * 获取任务状态码
     */
    public TorrentStateCode getStateCode() {
        if (!TorrentEngine.getInstance().isRunning())
            return TorrentStateCode.STOPPED;

        if (isPaused())
            return TorrentStateCode.PAUSED;

        if (!mTorrentHandle.isValid())
            return TorrentStateCode.ERROR;

        TorrentStatus status = mTorrentHandle.status();
        boolean isPaused = status.flags().and_(TorrentFlags.PAUSED).nonZero();

        if (isPaused && status.isFinished())
            return TorrentStateCode.FINISHED;

        if (isPaused && !status.isFinished())
            return TorrentStateCode.PAUSED;

        if (!isPaused && status.isFinished())
            return TorrentStateCode.SEEDING;

        TorrentStatus.State stateCode = status.state();
        switch (stateCode) {
            case CHECKING_FILES:
                return TorrentStateCode.CHECKING;
            case DOWNLOADING:
                return TorrentStateCode.DOWNLOADING;
            case FINISHED:
                return TorrentStateCode.FINISHED;
            case SEEDING:
                return TorrentStateCode.SEEDING;
            case ALLOCATING:
                return TorrentStateCode.ALLOCATING;
            case CHECKING_RESUME_DATA:
                return TorrentStateCode.CHECKING;
            case UNKNOWN:
                return TorrentStateCode.UNKNOWN;
            default:
                return TorrentStateCode.UNKNOWN;
        }
    }

    /**
     * 是否已暂停
     */
    public boolean isPaused() {
        boolean isPaused = mTorrentHandle.status(true).flags().and_(TorrentFlags.PAUSED).nonZero();
        return mTorrentHandle.isValid()
                && (isPaused
                || TorrentEngine.getInstance().isPaused()
                || !TorrentEngine.getInstance().isRunning());
    }

    /**
     * 是否正在做种
     */
    public boolean isSeeding() {
        return mTorrentHandle.isValid() && mTorrentHandle.status().isSeeding();
    }

    /**
     * 是否已完成
     */
    public boolean isFinished() {
        return mTorrentHandle.isValid() && mTorrentHandle.status().isFinished();
    }

    /**
     * 检查错误
     */
    private void checkError(Alert<?> alert) {
        switch (alert.type()) {
            case TORRENT_ERROR: {
                TorrentErrorAlert errorAlert = (TorrentErrorAlert) alert;
                ErrorCode error = errorAlert.error();
                String errorMsg = "";
                String filename = errorAlert.filename().substring(
                        errorAlert.filename().lastIndexOf("/") + 1);
                if (errorAlert.filename() != null)
                    errorMsg = "[" + filename + "] ";
                errorMsg += TorrentUtil.getErrorMsg(error);
                engineCallback.onTorrentError(mTorrent.getHash(), errorMsg);
                break;
            }
            case FILE_ERROR: {
                FileErrorAlert fileErrorAlert = (FileErrorAlert) alert;
                ErrorCode error = fileErrorAlert.error();
                String filename = fileErrorAlert.filename().substring(
                        fileErrorAlert.filename().lastIndexOf("/") + 1);
                String errorMsg = "[" + filename + "] " +
                        TorrentUtil.getErrorMsg(error);
                engineCallback.onTorrentError(mTorrent.getHash(), errorMsg);
                break;
            }
        }
    }

    /**
     * 保存数据
     */
    public void saveResumeData(boolean force) {
        long now = System.currentTimeMillis();

        if (force || (now - lastSaveResumeTime) >= SAVE_RESUME_SYNC_TIME) {
            lastSaveResumeTime = now;
        } else {
            //保存过快
            return;
        }

        try {
            if (mTorrentHandle != null && mTorrentHandle.isValid()) {
                mTorrentHandle.saveResumeData(TorrentHandle.SAVE_INFO_DICT);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error triggering resume data of " + mTorrent + ":");
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * 种子已移除
     */
    private void torrentRemoved() {
        if (engineCallback != null)
            engineCallback.onTorrentRemoved(mTorrent.getHash());

        TorrentEngine.getInstance().removeListener(taskListener);
    }

    /**
     * 保存恢复文件
     */
    private void serializeResumeData(SaveResumeDataAlert alert) {
        try {
            if (mTorrentHandle.isValid()) {
                byte_vector data = add_torrent_params.write_resume_data(alert.params().swig()).bencode();
                TorrentUtil.saveResumeData(Vectors.byte_vector2bytes(data));
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error saving resume data of " + mTorrent + ":");
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private final class TaskListener implements AlertListener {
        @Override
        public int[] types() {
            return INNER_LISTENER_TYPES;
        }

        @Override
        public void alert(Alert<?> alert) {
            if (!(alert instanceof TorrentAlert<?>))
                return;

            if (!((TorrentAlert<?>) alert).handle().swig().op_eq(mTorrentHandle.swig()))
                return;

            if (engineCallback == null)
                return;

            AlertType type = alert.type();
            switch (type) {
                case BLOCK_FINISHED:
                case STATE_CHANGED:
                    engineCallback.onTorrentStateChanged(mTorrent.getHash());
                    break;
                case TORRENT_FINISHED:
                    engineCallback.onTorrentFinished(mTorrent.getHash());
                    saveResumeData(true);
                    break;
                case TORRENT_REMOVED:
                    torrentRemoved();
                    break;
                case TORRENT_PAUSED:
                    engineCallback.onTorrentPaused(mTorrent.getHash());
                    break;
                case TORRENT_RESUMED:
                    engineCallback.onTorrentResumed(mTorrent.getHash());
                    break;
                case STATS:
                    engineCallback.onTorrentStateChanged(mTorrent.getHash());
                    break;
                case SAVE_RESUME_DATA:
                    serializeResumeData((SaveResumeDataAlert) alert);
                    break;
                case STORAGE_MOVED:
                    engineCallback.onTorrentMoved(mTorrent.getHash(), true);
                    saveResumeData(true);
                    break;
                case STORAGE_MOVED_FAILED:
                    engineCallback.onTorrentMoved(mTorrent.getHash(), false);
                    saveResumeData(true);
                    break;
                case PIECE_FINISHED:
                    saveResumeData(false);
                    break;
                default:
                    checkError(alert);
                    break;
            }
        }
    }
}
