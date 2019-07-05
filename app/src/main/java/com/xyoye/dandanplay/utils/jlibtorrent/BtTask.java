package com.xyoye.dandanplay.utils.jlibtorrent;

import android.media.MediaScannerConnection;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.AnnounceEntry;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.PieceFinishedAlert;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.CommonUtils;

import java.io.File;

import io.reactivex.annotations.Nullable;

import static com.frostwire.jlibtorrent.alerts.AlertType.ADD_TORRENT;
import static com.frostwire.jlibtorrent.alerts.AlertType.TRACKER_ERROR;

/**
 * Created by xyoye on 2019/6/11.
 * 下载任务的管理器
 * 每一项任务拥有单独的管理器
 */

public class BtTask extends SessionManager implements AlertListener{
    //块下载状态
    private BtFilePrices btFilePrices;
    //任务信息
    private Torrent torrent;
    //任务状态
    private TaskStatus taskStatus;
    //任务已完成且已刷新
    private boolean isRefreshAfterFinish;
    //是否为恢复任务
    private boolean isRecoveryTask = false;

    //控制块下载
    //子文件位置
    private int filePosition;
    //文件开始位置，需要下载的大小
    private long offset, size;
    //请求的下载块已经下载完成
    private boolean queryPriceResult = false;

    public BtTask(Torrent torrent) {
        super(false);
        this.torrent = torrent;
        //信息回调接口
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

    public BtTask(Torrent torrent, boolean isRecoveryTask) {
        this(torrent);
        this.isRecoveryTask = isRecoveryTask;
    }

    /**
     * 启动任务
     */
    public void startTask(){
        if (torrent.getTorrentFileList().size() < 1){
            ToastUtils.showShort("创建任务失败，子任务为0");
            return;
        }

        //下载的信息
        TorrentInfo torrentInfo = TorrentUtil.getTorrentInfoForFile(torrent.getTorrentPath());
        //保存文件夹
        File saveDirFile = new File(torrent.getSaveDirPath());
        //文件选择状态
        Priority[] priorities = new Priority[torrent.getTorrentFileList().size()];
        StringBuilder priorityBuilder = new StringBuilder();
        for (int i=0; i<torrent.getTorrentFileList().size(); i++){
            priorities[i] = torrent.getTorrentFileList().get(i).isChecked()
                                ? Priority.NORMAL
                                : Priority.IGNORE;
            priorityBuilder.append((torrent.getTorrentFileList().get(i).isChecked())
                                ? ("1;")
                                : ("0;"));
        }

        if (!isRecoveryTask){
            String prioritiesSaveData =  priorityBuilder.substring(0, priorityBuilder.length()-1);
            TorrentUtil.insertDBTorrent(torrent.getTorrentPath(), torrent.getAnimeTitle(), torrent.getMagnet(), prioritiesSaveData);
        }

        //开始下载，根据alert状态判断是否开始下载，获取下载进度
        download(torrentInfo, saveDirFile, null, priorities, null);
    }

    public @Nullable Torrent getTorrent() {
        return torrent;
    }

    public TaskStatus getTaskStatus() {
        if (taskStatus == null) {
            return TaskStatus.CHECKING;
        }

        //结束标志的判断要除初始标志之前
        if (torrent.isFinished() || taskStatus == TaskStatus.FINISHED){
            return TaskStatus.FINISHED;
        }

        //session断开视为任务停止
        if (!this.isRunning()) {
            return TaskStatus.STOPPED;
        }

        //session暂停视为任务暂停
        if (this.isPaused()) {
            return TaskStatus.PAUSED;
        }
        return taskStatus;
    }

    public boolean isFinished(){
        return torrent.isFinished();
    }

    public boolean isRefreshAfterFinish() {
        return isRefreshAfterFinish;
    }

    public void setRefreshAfterFinish(boolean refreshAfterFinish) {
        TorrentUtil.updateDBTorrentFinish(torrent.getTorrentPath());
        isRefreshAfterFinish = refreshAfterFinish;
    }

    public void setQueryPrice(int filePosition, long offset, long size){
        this.filePosition = filePosition;
        this.offset = offset;
        this.size = size;
    }

    public boolean getQueryPriceResult(){
        return queryPriceResult;
    }

    /**
     * 通知系统刷新已下载完成的文件
     */
    private void refreshFinishFile(){
        String[] paths = new String[torrent.getTorrentFileList().size()];
        String[] mimeTypes = new String[paths.length];
        for (int i = 0; i < torrent.getTorrentFileList().size(); i++) {
            paths[i] = torrent.getTorrentFileList().get(i).getPath();
            if (CommonUtils.isMediaFile(paths[i])){
                String ext = FileUtils.getFileExtension(paths[i]);
                mimeTypes[i] = "video/"+ext;
            }else {
                mimeTypes[i] = "";
            }
        }

        MediaScannerConnection.scanFile(IApplication.get_context(), paths, mimeTypes, (path, uri) ->
                LogUtils.d("system scan file completed"));
    }

    @Override
    public int[] types() {
        //不限制接收的alert
        return null;
    }

    @Override
    public void alert(Alert<?> alert) {
        //由于TRACKER_ERROR的alert过多且无用，不接收
        if (alert.type() == TRACKER_ERROR) return;

        //已成功添加到任务中
        if (alert.type() == ADD_TORRENT){
            TorrentHandle torrentHandle = ((TorrentAlert) alert).handle();
            //将tracker加入任务
            for (String trackerStr : IApplication.trackers){
                torrentHandle.addTracker(new AnnounceEntry(trackerStr));
            }
            //加入任务集合
            IApplication.taskList.add(this);
            //把hash和在任务集合中的位置加入map
            IApplication.taskMap.put(torrent.getHash(), IApplication.taskList.size()-1);
            //初始所有块的状态
            int fileCount = torrent.getTorrentFileList().size() - 1;
            long endFileSize = torrentHandle.torrentFile().files().fileSize(fileCount) - 1;
            int startIndex = torrentHandle.torrentFile().mapFile(0, 0, 1).piece();
            int endIndex = torrentHandle.torrentFile().mapFile(fileCount, endFileSize, 1).piece();
            btFilePrices = new BtFilePrices(startIndex, endIndex);
            torrentHandle.resume();
        }else {
            if (!(alert instanceof TorrentAlert<?>)) {
                return;
            }
            TorrentHandle torrentHandle = ((TorrentAlert) alert).handle();
            if (!((TorrentAlert<?>) alert).handle().swig().op_eq(torrentHandle.swig())) {
                return;
            }

            //根据alert类型更新下载进度及下载状态
            switch (alert.type()) {
                case TORRENT_CHECKED:
                    if (isRecoveryTask){
                        isRecoveryTask = false;
                        pause();
                    }
                    break;
                case TORRENT_FINISHED :
                    pause();
                    torrent.setFinished(true);
                    taskStatus = TaskStatus.FINISHED;
                    btFilePrices.setDownloadOver();
                    queryPriceResult = true;
                    refreshFinishFile();
                    break;
                case STATE_CHANGED:
                    taskStatus = getState(torrentHandle);
                    break;
                case PIECE_FINISHED:
                    if (alert instanceof PieceFinishedAlert){
                        //更新下载块的状态
                        PieceFinishedAlert pieceFinishedAlert = (PieceFinishedAlert)alert;
                        int priceIndex = pieceFinishedAlert.pieceIndex();
                        btFilePrices.setPriceDownloaded(priceIndex);

                        int queryStartIndex = torrentHandle.torrentFile().mapFile(filePosition, offset, 1).piece();
                        int queryEndIndex = torrentHandle.torrentFile().mapFile(filePosition, offset+size, 1).piece();
                        boolean allDownloaded = true;
                        for (int i=queryStartIndex; i<=queryEndIndex; i++){
                            if (!btFilePrices.isPriceDownload(i)){
                                allDownloaded = false;
                                //设置块优先级为最高
                                torrentHandle.piecePriority(i, Priority.SEVEN);
                                torrentHandle.setPieceDeadline(i, 1000);

                                torrentHandle.piecePriority(i + (queryStartIndex - queryEndIndex), Priority.SIX);
                                torrentHandle.setPieceDeadline(i + (queryStartIndex - queryEndIndex),1000);
                            }
                        }
                        queryPriceResult = allDownloaded;
                        if (queryPriceResult){
                            offset += size;
                        }
                    }
                    //不加break，因为后续动作也是要执行的
                default:
                    torrent.setDownloaded(torrentHandle.status().totalDone());
                    torrent.setDownloadRate(torrentHandle.status().downloadRate());
                    //获取子文件下载进度
                    long[] progress = torrentHandle.fileProgress();
                    //更新子文件下载进度，忽略下载的文件进度为0
                    if (progress.length == torrent.getTorrentFileList().size()){
                        for (int i=0; i<torrent.getTorrentFileList().size(); i++){
                            Torrent.TorrentFile torrentFile = torrent.getTorrentFileList().get(i);
                            torrentFile.setDownloaded(progress[i]);
                        }
                    }
                    break;
            }
        }
    }

    private TaskStatus getState(TorrentHandle torrentHandle) {

        if (torrent.isFinished()){
            return TaskStatus.FINISHED;
        }

        if (!torrentHandle.isValid()) {
            return TaskStatus.ERROR;
        }

        final TorrentStatus status = torrentHandle.status();
        final boolean isPaused = status.flags().and_(TorrentFlags.PAUSED).nonZero();

        if (isPaused && status.isFinished()) {
            return TaskStatus.FINISHED;
        }

        if (isPaused && !status.isFinished()) {
            return TaskStatus.PAUSED;
        }

        if (!isPaused && status.isFinished()) { // see the docs of isFinished
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
