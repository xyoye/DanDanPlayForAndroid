package com.xyoye.dandanplay.torrent;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.xyoye.dandanplay.torrent.info.Torrent;
import com.xyoye.dandanplay.torrent.info.TorrentPeerInfo;
import com.xyoye.dandanplay.torrent.info.TorrentStreamInfo;
import com.xyoye.dandanplay.torrent.utils.TorrentEngineCallback;
import com.xyoye.dandanplay.torrent.utils.TorrentStateCode;
import com.xyoye.dandanplay.torrent.utils.TorrentUtils;

import org.libtorrent4j.AlertListener;
import org.libtorrent4j.AnnounceEntry;
import org.libtorrent4j.ErrorCode;
import org.libtorrent4j.FileStorage;
import org.libtorrent4j.MoveFlags;
import org.libtorrent4j.PieceIndexBitfield;
import org.libtorrent4j.Priority;
import org.libtorrent4j.SessionHandle;
import org.libtorrent4j.TorrentFlags;
import org.libtorrent4j.TorrentHandle;
import org.libtorrent4j.TorrentInfo;
import org.libtorrent4j.TorrentStatus;
import org.libtorrent4j.Vectors;
import org.libtorrent4j.WebSeedEntry;
import org.libtorrent4j.alerts.Alert;
import org.libtorrent4j.alerts.AlertType;
import org.libtorrent4j.alerts.FileErrorAlert;
import org.libtorrent4j.alerts.SaveResumeDataAlert;
import org.libtorrent4j.alerts.TorrentAlert;
import org.libtorrent4j.alerts.TorrentErrorAlert;
import org.libtorrent4j.swig.add_torrent_params;
import org.libtorrent4j.swig.byte_vector;
import org.libtorrent4j.swig.peer_info_vector;
import org.libtorrent4j.swig.torrent_handle;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by xyoye on 2019/8/20.
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

    //每次保存需要间隔10秒
    private static final long SAVE_RESUME_SYNC_TIME = 10000;
    //预加载块数量
    private final static int PRELOAD_PIECES_COUNT = 5;

    private static final double MAX_RATIO = 9999;
    private static final int DEFAULT_PIECE_DEADLINE = 1000;

    private Torrent torrent;
    private TorrentHandle torrentHandle;
    private TorrentEngineCallback engineCallback;
    private TaskListener taskListener;
    //准备移除的未完成的文件
    private Set<File> incompleteFilesToRemove;

    private long lastSaveResumeTime;

    public TorrentTask(Torrent torrent, TorrentHandle torrentHandle, TorrentEngineCallback engineCallback) {
        this.torrent = torrent;
        this.torrentHandle = torrentHandle;
        this.engineCallback = engineCallback;
        this.taskListener = new TaskListener();
        TorrentEngine.getInstance().addListener(taskListener);
    }

    /**
     * 暂停
     */
    public void pause() {
        if (!torrentHandle.isValid())
            return;

        torrentHandle.unsetFlags(TorrentFlags.AUTO_MANAGED);
        torrentHandle.pause();
        saveResumeData(true);
    }

    /**
     * 继续
     */
    public void resume() {
        if (!torrentHandle.isValid())
            return;

        if (TorrentEngine.getInstance().getEngineSettings().autoManaged)
            torrentHandle.setFlags(TorrentFlags.AUTO_MANAGED);
        else
            torrentHandle.unsetFlags(TorrentFlags.AUTO_MANAGED);
        torrentHandle.resume();
        saveResumeData(true);
    }


    /**
     * 提高块的优先级
     */
    public void setInterestedPieces(TorrentStreamInfo stream, int startPiece, int numPieces) {
        if (stream == null || startPiece < 0 || numPieces < 0)
            return;

        for (int i = 0; i < numPieces; i++) {
            int piece = startPiece + i;
            if (piece > stream.lastFilePiece)
                break;

            if (i + 1 == numPieces) {
                int preloadPieces = PRELOAD_PIECES_COUNT;
                for (int p = piece; p <= stream.lastFilePiece; p++) {
                    //将第一个找的未完成的块优先级设为最高
                    if (!torrentHandle.havePiece(p)) {
                        torrentHandle.piecePriority(p, Priority.TOP_PRIORITY);
                        torrentHandle.setPieceDeadline(p, DEFAULT_PIECE_DEADLINE);
                        preloadPieces--;
                        if (preloadPieces == 0)
                            break;
                    }
                }

            } else {
                if (!torrentHandle.havePiece(piece)) {
                    torrentHandle.piecePriority(piece, Priority.TOP_PRIORITY);
                    torrentHandle.setPieceDeadline(piece, DEFAULT_PIECE_DEADLINE);
                }
            }
        }
    }

    /**
     * 设置自动管理任务
     */
    public void setAutoManaged(boolean autoManaged) {
        if (isPaused())
            return;

        if (autoManaged)
            torrentHandle.setFlags(TorrentFlags.AUTO_MANAGED);
        else
            torrentHandle.unsetFlags(TorrentFlags.AUTO_MANAGED);
    }

    /**
     * 设置最大连接数
     */
    public void setMaxConnections(int connections) {
        if (!torrentHandle.isValid())
            return;
        torrentHandle.swig().set_max_connections(connections);
    }

    /**
     * 设置最大上传数
     */
    public void setMaxUploads(int uploads) {
        if (!torrentHandle.isValid())
            return;
        torrentHandle.swig().set_max_uploads(uploads);
    }

    /**
     * 设置下载速度上限
     */
    public void setDownloadSpeedLimit(int limit) {
        torrentHandle.setDownloadLimit(limit);
        saveResumeData(true);
    }

    /**
     * 设置上传速度上限
     */
    public void setUploadSpeedLimit(int limit) {
        torrentHandle.setUploadLimit(limit);
        saveResumeData(true);
    }

    /**
     * 设置任务种子
     */
    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }

    /**
     * 设置下载目录
     */
    public void setDownloadDir(String path) {
        try {
            torrentHandle.moveStorage(path, MoveFlags.ALWAYS_REPLACE_FILES);

        } catch (Exception e) {
            Log.e(TAG, "Error changing save path: ");
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * 读取块
     */
    public void readPiece(int pieceIndex) {
        torrentHandle.readPiece(pieceIndex);
    }

    /**
     * 设置所有文件的下载优先级
     */
    public void prioritizeFiles(Priority[] priorities) {
        if (torrentHandle == null || !torrentHandle.isValid())
            return;

        TorrentInfo ti = torrentHandle.torrentFile();
        if (ti == null)
            return;

        if (priorities != null) {
            //优先级配置数量与文件数量不符
            if (ti.numFiles() != priorities.length)
                return;

            torrentHandle.prioritizeFiles(priorities);

        } else {
            //设置所有文件的优先级为默认
            final Priority[] wholeTorrentPriorities =
                    Priority.array(Priority.DEFAULT, ti.numFiles());

            torrentHandle.prioritizeFiles(wholeTorrentPriorities);
        }
    }

    /**
     * 设置为顺序下载
     */
    public void setSequentialDownload(boolean sequential) {
        if (sequential)
            torrentHandle.setFlags(TorrentFlags.SEQUENTIAL_DOWNLOAD);
        else
            torrentHandle.unsetFlags(TorrentFlags.SEQUENTIAL_DOWNLOAD);
    }

    /**
     * 移除任务
     */
    public void remove(boolean withFiles) {
        incompleteFilesToRemove = getIncompleteFiles();

        if (torrentHandle.isValid()) {
            if (withFiles)
                TorrentEngine.getInstance().remove(torrentHandle, SessionHandle.DELETE_FILES);
            else
                TorrentEngine.getInstance().remove(torrentHandle);
        }
    }

    /**
     * 请求跟踪程序
     */
    public void requestTrackerAnnounce() {
        torrentHandle.forceReannounce();
    }

    /**
     * 请求跟踪者类别
     */
    public void requestTrackerScrape() {
        torrentHandle.scrapeTracker();
    }

    /**
     * 强制检查
     */
    public void forceRecheck() {
        torrentHandle.forceRecheck();
    }

    /**
     * 替换tracker
     */
    public void replaceTrackers(Set<String> trackers) {
        List<AnnounceEntry> urls = new ArrayList<>(trackers.size());
        for (String url : trackers)
            urls.add(new AnnounceEntry(url));
        torrentHandle.replaceTrackers(urls);
        saveResumeData(true);
    }

    /**
     * 增加tracker
     */
    public void addTrackers(Set<String> trackers) {
        for (String url : trackers)
            torrentHandle.addTracker(new AnnounceEntry(url));
        saveResumeData(true);
    }

    /**
     * 增加tracker
     */
    public void addTrackers(List<AnnounceEntry> trackers) {
        for (AnnounceEntry tracker : trackers)
            torrentHandle.addTracker(tracker);
        saveResumeData(true);
    }

    /**
     * 增加WebSeed
     */
    public void addWebSeeds(List<WebSeedEntry> webSeeds) {
        for (WebSeedEntry webSeed : webSeeds) {
            if (webSeed == null)
                continue;

            switch (webSeed.type()) {
                case HTTP_SEED:
                    torrentHandle.addHttpSeed(webSeed.url());
                    break;
                case URL_SEED:
                    torrentHandle.addUrlSeed(webSeed.url());
                    break;
            }
        }
    }

    /**
     * 获取块的位值
     */
    public boolean[] pieces() {
        PieceIndexBitfield bitfield = torrentHandle.status(TorrentHandle.QUERY_PIECES).pieces();
        boolean[] pieces = new boolean[bitfield.size()];
        for (int i = 0; i < bitfield.size(); i++)
            pieces[i] = bitfield.getBit(i);

        return pieces;
    }

    /**
     * 生成magnet链接
     */
    public String makeMagnet(boolean includePriorities) {
        if (!torrentHandle.isValid())
            return null;

        String uri = torrentHandle.makeMagnetUri();

        if (includePriorities) {
            String indices = getFileIndicesBep53(torrentHandle.filePriorities());
            if (!TextUtils.isEmpty(indices))
                uri += "&so=" + indices;
        }

        return uri;
    }

    /**
     * 索引转String
     */
    private static String indicesToStr(int startIndex, int endIndex) {
        if (startIndex == -1 || endIndex == -1)
            return null;

        return (startIndex == endIndex ?
                Integer.toString(endIndex) :
                String.format(Locale.ENGLISH, "%d-%d", startIndex, endIndex));
    }

    /**
     * 获取下载种子
     */
    public Torrent getTorrent() {
        return torrent;
    }

    /**
     * 获取下载进度
     */
    public int getProgress() {
        if (torrentHandle == null || !torrentHandle.isValid())
            return 0;

        if (torrentHandle.status() == null)
            return 0;

        float fp = torrentHandle.status().progress();
        TorrentStatus.State state = torrentHandle.status().state();
        if (Float.compare(fp, 1f) == 0 && state != TorrentStatus.State.CHECKING_FILES)
            return 100;

        int p = (int) (torrentHandle.status().progress() * 100);
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
     * 获取子文件的下载进度
     */
    public long[] getChildFileProgress(){
        return torrentHandle.fileProgress();
    }

    /**
     * 获取需要下载的总大小
     */
    public long getSize() {
        if (!torrentHandle.isValid())
            return 0;

        TorrentInfo info = torrentHandle.torrentFile();

        return info != null ? info.totalSize() : 0;
    }

    /**
     * 获取种子hash
     */
    public String getInfoHash() {
        return torrentHandle.infoHash().toString();
    }

    /**
     * 获取下载速度上限
     */
    public int getDownloadSpeedLimit() {
        return torrentHandle.isValid() ? torrentHandle.getDownloadLimit() : 0;
    }

    /**
     * 获取上传速度上限
     */
    public int getUploadSpeedLimit() {
        return torrentHandle.isValid() ? torrentHandle.getUploadLimit() : 0;
    }

    /**
     * 获取有效的已下载的大小
     */
    public long getReceivedBytes() {
        return torrentHandle.isValid() ? torrentHandle.status().totalPayloadDownload() : 0;
    }

    /**
     * 获取已下载的总下载大小
     */
    public long getTotalReceivedBytes() {
        return torrentHandle.isValid() ? torrentHandle.status().allTimeDownload() : 0;
    }

    /**
     * 获取子文件已下载大小
     */
    public long[] getFilesReceivedBytes() {
        if (!torrentHandle.isValid()) {
            return null;
        }

        return torrentHandle.fileProgress(TorrentHandle.FileProgressFlags.PIECE_GRANULARITY);
    }

    /**
     * 获取已上传的有效的大小
     */
    public long getSentBytes() {
        return torrentHandle.isValid() ? torrentHandle.status().totalPayloadUpload() : 0;
    }

    /**
     * 获取已上传的总大小
     */
    public long getTotalSentBytes() {
        return torrentHandle.isValid() ? torrentHandle.status().allTimeUpload() : 0;
    }

    /**
     * 获取希望下载的总大小
     */
    public long getTotalWanted() {
        return torrentHandle.isValid() ? torrentHandle.status().totalWanted() : 0;
    }

    /**
     * 获取已连接的用户数量
     */
    public int getConnectedPeers() {
        return torrentHandle.isValid() ? torrentHandle.status().numPeers() : 0;
    }

    /**
     * 获取已连接的种子数量
     */
    public int getConnectedSeeds() {
        return torrentHandle.isValid() ? torrentHandle.status().numSeeds() : 0;
    }


    /**
     * 获取下载速度
     */
    public long getDownloadSpeed() {
        return (!torrentHandle.isValid() || isFinished() || isPaused() || isSeeding()) ? 0 : torrentHandle.status().downloadPayloadRate();
    }

    /**
     * 获取可供连接的用户数量
     */
    public int getTotalPeers() {
        if (!torrentHandle.isValid())
            return 0;

        TorrentStatus ts = torrentHandle.status();
        int peers = ts.numComplete() + ts.numIncomplete();

        return (peers > 0 ? peers : torrentHandle.status().listPeers());
    }

    /**
     * 获取已完成上传者数量
     */
    public int getTotalSeeds() {
        return torrentHandle.isValid() ? torrentHandle.status().listSeeds() : 0;
    }


    /**
     * 获取上传速度
     */
    public long getUploadSpeed() {
        return (!torrentHandle.isValid() || (isFinished() && !isSeeding()) || isPaused()) ? 0 : torrentHandle.status().uploadPayloadRate();
    }

    /**
     * 获取最大连接数
     */
    public int getMaxConnections() {
        if (!torrentHandle.isValid())
            return -1;

        return torrentHandle.swig().max_connections();
    }

    /**
     * 获取最大连接数
     */
    public int getMaxUploads() {
        if (!torrentHandle.isValid())
            return -1;

        return torrentHandle.swig().max_uploads();
    }

    /**
     * 获取可用性
     */
    public double getAvailability(int[] piecesAvailability) {
        if (piecesAvailability == null || piecesAvailability.length == 0)
            return 0;

        int min = Integer.MAX_VALUE;
        for (int avail : piecesAvailability)
            if (avail < min)
                min = avail;

        int total = 0;
        for (int avail : piecesAvailability)
            if (avail > 0 && avail > min)
                ++total;

        return (total / (double) piecesAvailability.length) + min;
    }

    /**
     * 获取每个文件的可用性
     */
    public double[] getFilesAvailability(int[] piecesAvailability) {
        if (!torrentHandle.isValid())
            return new double[0];

        TorrentInfo ti = torrentHandle.torrentFile();
        if (ti == null)
            return new double[0];
        int numFiles = ti.numFiles();
        if (numFiles < 0)
            return new double[0];

        double[] filesAvail = new double[numFiles];
        if (piecesAvailability == null || piecesAvailability.length == 0) {
            Arrays.fill(filesAvail, -1);

            return filesAvail;
        }
        for (int i = 0; i < numFiles; i++) {
            Pair<Integer, Integer> filePieces = getFilePieces(ti, i);
            if (filePieces == null) {
                filesAvail[i] = -1;
                continue;
            }
            int availablePieces = 0;
            for (int p = filePieces.first; p <= filePieces.second; p++)
                availablePieces += (piecesAvailability[p] > 0 ? 1 : 0);
            filesAvail[i] = (double) availablePieces / (filePieces.second - filePieces.first + 1);
        }

        return filesAvail;
    }

    /**
     * 获取块可用性
     */
    public int[] getPiecesAvailability() {
        if (!torrentHandle.isValid())
            return new int[0];

        PieceIndexBitfield pieces = torrentHandle.status(TorrentHandle.QUERY_PIECES).pieces();
        List<TorrentPeerInfo> peers = getTorrentPeerInfo();
        int[] avail = new int[pieces.size()];
        for (int i = 0; i < pieces.size(); i++)
            avail[i] = (pieces.getBit(i) ? 1 : 0);

        for (TorrentPeerInfo peer : peers) {
            PieceIndexBitfield peerPieces = peer.pieces();
            for (int i = 0; i < pieces.size(); i++)
                if (peerPieces.getBit(i))
                    ++avail[i];
        }

        return avail;
    }

    /**
     * 获取优先级高于默认的索引
     */
    public static String getFileIndicesBep53(Priority[] priorities) {
        ArrayList<String> buf = new ArrayList<>();
        int startIndex = -1;
        int endIndex = -1;

        String indicesStr;
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].swig() == Priority.IGNORE.swig()) {
                if ((indicesStr = indicesToStr(startIndex, endIndex)) != null)
                    buf.add(indicesStr);
                startIndex = -1;

            } else {
                endIndex = i;
                if (startIndex == -1)
                    startIndex = endIndex;
            }
        }
        if ((indicesStr = indicesToStr(startIndex, endIndex)) != null)
            buf.add(indicesStr);

        return TextUtils.join(",", buf);
    }

    /**
     * 获取tracker集合
     */
    public Set<String> getTrackersUrl() {
        if (!torrentHandle.isValid())
            return new HashSet<>();

        List<AnnounceEntry> trackers = torrentHandle.trackers();
        Set<String> urls = new HashSet<>(trackers.size());

        for (AnnounceEntry entry : trackers)
            urls.add(entry.url());

        return urls;
    }

    /**
     * 获取tracker集合
     */
    public List<AnnounceEntry> getTrackers() {
        if (!torrentHandle.isValid())
            return new ArrayList<>();

        return torrentHandle.trackers();
    }

    /**
     * 获取子文件
     */
    public File getFile(int fileIndex) {
        return new File(torrentHandle.savePath() + "/" + torrentHandle.torrentFile().files().filePath(fileIndex));
    }

    /**
     * 获取流信息
     */
    public TorrentStreamInfo getStreamInfo(int fileIndex) {
        TorrentInfo ti = torrentHandle.torrentFile();
        FileStorage fs = ti.files();
        Pair<Integer, Integer> filePieces = getFilePieces(ti, fileIndex);
        if (filePieces == null)
            throw new IllegalArgumentException("Incorrect file index");

        return new TorrentStreamInfo(torrent.getTorrentHash(), fileIndex,
                filePieces.first, filePieces.second, ti.pieceLength(),
                fs.fileOffset(fileIndex), fs.fileSize(fileIndex),
                ti.pieceSize(filePieces.second));

    }

    /**
     * 获取块集合
     */
    public List<TorrentPeerInfo> getTorrentPeerInfo() {
        if (!torrentHandle.isValid())
            return new ArrayList<>();

        torrent_handle th_swig = torrentHandle.swig();
        peer_info_vector v = new peer_info_vector();
        th_swig.get_peer_info(v);

        int size = (int) v.size();
        ArrayList<TorrentPeerInfo> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            l.add(new TorrentPeerInfo(v.get(i)));

        return l;
    }

    /**
     * 获取未完成的文件
     */
    public Set<File> getIncompleteFiles() {
        Set<File> s = new HashSet<>();
        try {
            if (!torrentHandle.isValid())
                return s;

            long[] progress = torrentHandle.fileProgress(TorrentHandle.FileProgressFlags.PIECE_GRANULARITY);
            TorrentInfo ti = torrentHandle.torrentFile();
            FileStorage fs = ti.files();
            String prefix = torrent.getSaveDirPath();
            File torrentFile = new File(torrent.getTorrentFilePath());
            if (!torrentFile.exists())
                return s;
            long createdTime = torrentFile.lastModified();

            for (int i = 0; i < progress.length; i++) {
                String filePath = fs.filePath(i);
                long fileSize = fs.fileSize(i);
                if (progress[i] < fileSize) {
                    /* Lets see if indeed the file is incomplete */
                    File f = new File(prefix, filePath);
                    if (!f.exists())
                        /* Nothing to do here */
                        continue;

                    if (f.lastModified() >= createdTime)
                        /* We have a file modified (supposedly) by this transfer */
                        s.add(f);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error calculating the incomplete files set of " + torrent.getTorrentHash());
        }

        return s;
    }

    /**
     * 获取已完成的块数量
     */
    public int getNumDownloadedPieces() {
        return torrentHandle.isValid() ? torrentHandle.status().numPieces() : 0;
    }

    /**
     * 获取种子状态
     */
    public TorrentStatus getTorrentStatus() {
        return torrentHandle.isValid() ? torrentHandle.status() : null;
    }

    /**
     * 获取活动时间
     */
    public long getActiveTime() {
        return torrentHandle.isValid() ? torrentHandle.status().activeDuration() / 1000L : 0;
    }

    /**
     * 获取上传时间
     */
    public long getSeedingTime() {
        return torrentHandle.isValid() ? torrentHandle.status().seedingDuration() / 1000L : 0;
    }

    /**
     * 获取预计完成时间
     */
    public long getETA() {
        if (!torrentHandle.isValid())
            return 0;
        if (getStateCode() != TorrentStateCode.DOWNLOADING)
            return 0;

        TorrentInfo ti = torrentHandle.torrentFile();
        if (ti == null)
            return 0;
        TorrentStatus status = torrentHandle.status();
        long left = ti.totalSize() - status.totalDone();
        long rate = status.downloadPayloadRate();
        if (left <= 0)
            return 0;
        if (rate <= 0)
            return -1;

        return left / rate;
    }

    /**
     * 获取上传比例
     */
    public double getShareRatio() {
        if (!torrentHandle.isValid())
            return 0;

        long uploaded = getTotalSentBytes();
        long allTimeReceived = getTotalReceivedBytes();
        long totalDone = torrentHandle.status().totalDone();

        //存在任务状态丢失或导入完成99%的任务的情况
        long downloaded = (allTimeReceived < totalDone * 0.01 ? totalDone : allTimeReceived);
        if (downloaded == 0)
            return (uploaded == 0 ? 0.0 : MAX_RATIO);
        double ratio = (double) uploaded / (double) downloaded;

        return (ratio > MAX_RATIO ? MAX_RATIO : ratio);
    }

    /**
     * 获取种子详情信息
     */
    public TorrentInfo getTorrentInfo() {
        return torrentHandle.torrentFile();
    }

    /**
     * 获取任务状态码
     */
    public TorrentStateCode getStateCode() {
        if (!TorrentEngine.getInstance().isRunning())
            return TorrentStateCode.STOPPED;

        if (isPaused())
            return TorrentStateCode.PAUSED;

        if (!torrentHandle.isValid())
            return TorrentStateCode.ERROR;

        TorrentStatus status = torrentHandle.status();
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
     * 是否自动管理任务
     */
    public boolean isAutoManaged() {
        return torrentHandle.isValid() && torrentHandle.status().flags().and_(TorrentFlags.AUTO_MANAGED).nonZero();
    }

    /**
     * 是否已暂停
     */
    public boolean isPaused() {
        boolean isPaused = torrentHandle.status(true).flags().and_(TorrentFlags.PAUSED).nonZero();
        return torrentHandle.isValid()
                && (isPaused
                || TorrentEngine.getInstance().isPaused()
                || !TorrentEngine.getInstance().isRunning());
    }

    /**
     * 是否正在做种
     */
    public boolean isSeeding() {
        return torrentHandle.isValid() && torrentHandle.status().isSeeding();
    }

    /**
     * 是否已完成
     */
    public boolean isFinished() {
        return torrentHandle.isValid() && torrentHandle.status().isFinished();
    }

    /**
     * 是否正在下载中
     */
    public boolean isDownloading() {
        return getDownloadSpeed() > 0;
    }

    /**
     * 是否为顺序下载
     */
    public boolean isSequentialDownload() {
        return torrentHandle.isValid() && torrentHandle.status().flags().and_(TorrentFlags.SEQUENTIAL_DOWNLOAD).nonZero();
    }

    /**
     * 是否存在该块
     */
    public boolean havePiece(int pieceIndex) {
        return torrentHandle.havePiece(pieceIndex);
    }

    /**
     * 获取下载块
     */
    private Pair<Integer, Integer> getFilePieces(TorrentInfo ti, int fileIndex) {
        if (!torrentHandle.isValid())
            return null;

        if (fileIndex < 0 || fileIndex >= ti.numFiles())
            return null;
        FileStorage fs = ti.files();
        long fileSize = fs.fileSize(fileIndex);
        long fileOffset = fs.fileOffset(fileIndex);

        return new Pair<>((int) (fileOffset / ti.pieceLength()),
                (int) ((fileOffset + fileSize - 1) / ti.pieceLength()));
    }

    /**
     * 检查错误
     */
    private void checkError(Alert<?> alert) {
        switch (alert.type()) {
            case TORRENT_ERROR: {
                TorrentErrorAlert errorAlert = (TorrentErrorAlert) alert;
                ErrorCode error = errorAlert.error();
                if (error.isError()) {
                    String errorMsg = "";
                    String filename = errorAlert.filename().substring(
                            errorAlert.filename().lastIndexOf("/") + 1);
                    if (errorAlert.filename() != null)
                        errorMsg = "[" + filename + "] ";
                    errorMsg += TorrentUtils.getErrorMsg(error);
                    engineCallback.onTorrentError(torrent.getTorrentHash(), errorMsg);
                }
                break;
            }
            case FILE_ERROR: {
                FileErrorAlert fileErrorAlert = (FileErrorAlert) alert;
                ErrorCode error = fileErrorAlert.error();
                String filename = fileErrorAlert.filename().substring(
                        fileErrorAlert.filename().lastIndexOf("/") + 1);
                if (error.isError()) {
                    String errorMsg = "[" + filename + "] " +
                            TorrentUtils.getErrorMsg(error);
                    engineCallback.onTorrentError(torrent.getTorrentHash(), errorMsg);
                }
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
            if (torrentHandle != null && torrentHandle.isValid()) {
                torrentHandle.saveResumeData(TorrentHandle.SAVE_INFO_DICT);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error triggering resume data of " + torrent + ":");
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * 种子已移除
     */
    private void torrentRemoved() {
        if (engineCallback != null)
            engineCallback.onTorrentRemoved(torrent.getTorrentHash());

        TorrentEngine.getInstance().removeListener(taskListener);
        finalCleanup(incompleteFilesToRemove);
    }

    /**
     * 保存恢复文件
     */
    private void serializeResumeData(SaveResumeDataAlert alert) {
        try {
            if (torrentHandle.isValid()) {
                byte_vector data = add_torrent_params.write_resume_data(alert.params().swig()).bencode();
                TorrentUtils.saveResumeData(Vectors.byte_vector2bytes(data));
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error saving resume data of " + torrent + ":");
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * 删除文件
     */
    private void finalCleanup(Set<File> incompleteFiles) {
        if (incompleteFiles != null) {
            for (File f : incompleteFiles) {
                try {
                    if (f.exists() && !f.delete())
                        Log.w(TAG, "Can't delete file " + f);
                } catch (Exception e) {
                    Log.w(TAG, "Can't delete file " + f + ", ex: " + e.getMessage());
                }
            }
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

            if (!((TorrentAlert<?>) alert).handle().swig().op_eq(torrentHandle.swig()))
                return;

            if (engineCallback == null)
                return;

            AlertType type = alert.type();
            switch (type) {
                case BLOCK_FINISHED:
                case STATE_CHANGED:
                    engineCallback.onTorrentStateChanged(torrent.getTorrentHash());
                    break;
                case TORRENT_FINISHED:
                    engineCallback.onTorrentFinished(torrent.getTorrentHash());
                    saveResumeData(true);
                    break;
                case TORRENT_REMOVED:
                    torrentRemoved();
                    break;
                case TORRENT_PAUSED:
                    engineCallback.onTorrentPaused(torrent.getTorrentHash());
                    break;
                case TORRENT_RESUMED:
                    engineCallback.onTorrentResumed(torrent.getTorrentHash());
                    break;
                case STATS:
                    engineCallback.onTorrentStateChanged(torrent.getTorrentHash());
                    break;
                case SAVE_RESUME_DATA:
                    serializeResumeData((SaveResumeDataAlert) alert);
                    break;
                case STORAGE_MOVED:
                    engineCallback.onTorrentMoved(torrent.getTorrentHash(), true);
                    saveResumeData(true);
                    break;
                case STORAGE_MOVED_FAILED:
                    engineCallback.onTorrentMoved(torrent.getTorrentHash(), false);
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
