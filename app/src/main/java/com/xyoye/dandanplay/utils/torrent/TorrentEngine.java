package com.xyoye.dandanplay.utils.torrent;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.xyoye.dandanplay.utils.torrent.info.ProxyInfo;
import com.xyoye.dandanplay.utils.torrent.info.Torrent;
import com.xyoye.dandanplay.utils.torrent.utils.EngineSettings;
import com.xyoye.dandanplay.utils.torrent.utils.IPFilterParser;
import com.xyoye.dandanplay.utils.torrent.utils.TorrentConfig;
import com.xyoye.dandanplay.utils.torrent.utils.TorrentEngineCallback;
import com.xyoye.dandanplay.utils.torrent.utils.TorrentFileUtils;
import com.xyoye.dandanplay.utils.torrent.utils.TorrentUtils;

import org.apache.commons.io.FileUtils;
import org.libtorrent4j.AlertListener;
import org.libtorrent4j.ErrorCode;
import org.libtorrent4j.Priority;
import org.libtorrent4j.SessionHandle;
import org.libtorrent4j.SessionManager;
import org.libtorrent4j.SessionParams;
import org.libtorrent4j.SettingsPack;
import org.libtorrent4j.Sha1Hash;
import org.libtorrent4j.TorrentHandle;
import org.libtorrent4j.TorrentInfo;
import org.libtorrent4j.Vectors;
import org.libtorrent4j.alerts.Alert;
import org.libtorrent4j.alerts.AlertType;
import org.libtorrent4j.alerts.ListenFailedAlert;
import org.libtorrent4j.alerts.PortmapErrorAlert;
import org.libtorrent4j.alerts.SessionErrorAlert;
import org.libtorrent4j.alerts.TorrentAlert;
import org.libtorrent4j.alerts.TorrentRemovedAlert;
import org.libtorrent4j.swig.add_torrent_params;
import org.libtorrent4j.swig.bdecode_node;
import org.libtorrent4j.swig.byte_vector;
import org.libtorrent4j.swig.create_torrent;
import org.libtorrent4j.swig.entry;
import org.libtorrent4j.swig.error_code;
import org.libtorrent4j.swig.int_vector;
import org.libtorrent4j.swig.ip_filter;
import org.libtorrent4j.swig.libtorrent;
import org.libtorrent4j.swig.session_params;
import org.libtorrent4j.swig.settings_pack;
import org.libtorrent4j.swig.string_vector;
import org.libtorrent4j.swig.torrent_info;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xyoye on 2019/8/20.
 */

public class TorrentEngine extends SessionManager {
    private static final int[] INNER_LISTENER_TYPES = new int[]{
            AlertType.ADD_TORRENT.swig(),
            AlertType.TORRENT_REMOVED.swig(),
            AlertType.SESSION_ERROR.swig(),
            AlertType.PORTMAP_ERROR.swig(),
            AlertType.LISTEN_FAILED.swig()
    };

    private EngineSettings engineSettings;
    private EngineListener engineListener;
    private TorrentEngineCallback engineCallback;
    private ExecutorService loadTaskExecutor;
    private ConcurrentHashMap<String, TorrentTask> torrentTasks;
    private ConcurrentHashMap<String, byte[]> hashTorrentDataMap;
    private Map<String, Torrent> addTorrentsQueue;
    private ArrayList<String> hashList;
    private Queue<LoadTorrentTask> loadTaskQueue;

    private TorrentEngine() {
        engineListener = new EngineListener();
        torrentTasks = new ConcurrentHashMap<>();
        hashTorrentDataMap = new ConcurrentHashMap<>();
        hashList = new ArrayList<>();
        addTorrentsQueue = new HashMap<>();
        loadTaskQueue = new LinkedList<>();
        loadTaskExecutor = Executors.newCachedThreadPool();
    }

    public static TorrentEngine getInstance() {
        return EngineHolder.INSTANCE;
    }

    @Override
    protected void onBeforeStart() {
        addListener(engineListener);
    }

    @Override
    public void start() {
        SessionParams params = getSessionParams();
        settings_pack sp = params.settings().swig();
        sp.set_str(settings_pack.string_types.dht_bootstrap_nodes.swigValue(), getDHTBootstrapNodes());

        int[] versions = TorrentUtils.getVersions();
        String fingerprint = libtorrent.generate_fingerprint(TorrentUtils.PEER_FINGERPRINT,
                versions[0], versions[1], versions[2], 0);
        sp.set_str(settings_pack.string_types.peer_fingerprint.swigValue(), fingerprint);
        sp.set_str(settings_pack.string_types.user_agent.swigValue(), TorrentUtils.getUserAgent());

        super.start(params);
    }

    @Override
    protected void onAfterStart() {
        if (engineCallback != null)
            engineCallback.onEngineStarted();
    }

    @Override
    public boolean isPaused() {
        return super.isPaused();
    }

    @Override
    protected void onApplySettings(SettingsPack sp) {
        saveSessionSettings();
    }

    @Override
    protected void onBeforeStop() {
        saveAllResumeData();
        //必须销毁会话之前销毁Handles
        torrentTasks.clear();
        hashList.clear();
        hashTorrentDataMap.clear();
        removeListener(engineListener);
        saveSessionSettings();
    }

    /**
     * 暂停所有任务下载
     */
    public void pauseAll() {
        for (TorrentTask torrentTask : torrentTasks.values()) {
            if (torrentTask == null)
                continue;
            torrentTask.pause();
        }
    }

    /**
     * 恢复所有任务下载
     */
    public void resumeAll() {
        for (TorrentTask torrentTask : torrentTasks.values()) {
            if (torrentTask == null)
                continue;
            torrentTask.resume();
        }
    }

    /**
     * 下载，增加一个任务
     */
    public void download(Torrent torrent) {
        //任务已存在，不下载
        if (hashList.contains(torrent.getTorrentHash()))
            return;
        TorrentTask torrentTask = torrentTasks.get(torrent.getTorrentHash());
        if (torrentTask != null)
            return;

        //文件数与Priority数必须相等
        TorrentInfo torrentInfo = new TorrentInfo(new File(torrent.getTorrentFilePath()));
        Priority[] priorities = TorrentUtils.getPriorities(torrent);
        if (priorities == null || priorities.length != torrentInfo.numFiles())
            throw new IllegalArgumentException("File count doesn't match: " + torrent.getTaskName());

        File resumeFile = new File(TorrentFileUtils.TaskResumeFilePath);
        File saveDir = new File(torrent.getSaveDirPath());

        //添加任务的队列增加一项
        addTorrentsQueue.put(torrentInfo.infoHash().toString(), torrent);

        //开始下载，EngineListener等待任务添加结果
        download(torrentInfo, saveDir, resumeFile, priorities, null);
    }

    /**
     * 移除已有任务
     */
    public void cancelFetchMagnet(String infoHash) {
        if (infoHash == null || !hashList.contains(infoHash))
            return;

        hashList.remove(infoHash);
        TorrentHandle th = find(new Sha1Hash(infoHash));
        if (th != null && th.isValid())
            remove(th, SessionHandle.DELETE_FILES);
    }

    /**
     * 保存任务数据
     */
    public void saveAllResumeData() {
        for (TorrentTask task : torrentTasks.values()) {
            if (task == null)
                continue;
            task.saveResumeData(true);
        }
    }

    /**
     * 修改配置
     */
    public void setSettings(EngineSettings engineSettings) {
        this.engineSettings = engineSettings;
        applySettings(engineSettings);
    }

    /**
     * 应用设置
     */
    private void applySettingsPack(SettingsPack settingsPack) {
        if (settingsPack == null)
            return;

        applySettings(settingsPack);
        saveSessionSettings();
    }

    /**
     * 应用设置
     */
    private void applySettings(EngineSettings engineSettings) {
        if (engineSettings == null || !isRunning())
            return;

        SettingsPack settingsPack = settings();
        settingsToSettingsPack(engineSettings, settingsPack);
        applySettingsPack(settingsPack);
    }

    /**
     * 设置转换
     */
    private void settingsToSettingsPack(EngineSettings engineSettings, SettingsPack settingsPack) {
        settingsPack.cacheSize(engineSettings.cacheSize);
        settingsPack.activeDownloads(engineSettings.activeDownloads);
        settingsPack.activeSeeds(engineSettings.activeSeeds);
        settingsPack.activeLimit(engineSettings.activeLimit);
        settingsPack.maxPeerlistSize(engineSettings.maxPeerListSize);
        settingsPack.tickInterval(engineSettings.tickInterval);
        settingsPack.inactivityTimeout(engineSettings.inactivityTimeout);
        settingsPack.connectionsLimit(engineSettings.connectionsLimit);
        settingsPack.setString(settings_pack.string_types.listen_interfaces.swigValue(), "0.0.0.0:" + engineSettings.port);
        settingsPack.enableDht(engineSettings.dhtEnabled);
        settingsPack.broadcastLSD(engineSettings.lsdEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.enable_incoming_utp.swigValue(), engineSettings.utpEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.enable_outgoing_utp.swigValue(), engineSettings.utpEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.enable_upnp.swigValue(), engineSettings.upnpEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.enable_natpmp.swigValue(), engineSettings.natPmpEnabled);
        settingsPack.setInteger(settings_pack.int_types.in_enc_policy.swigValue(), settings_pack.enc_policy.pe_enabled.swigValue());
        settingsPack.setInteger(settings_pack.int_types.out_enc_policy.swigValue(), settings_pack.enc_policy.pe_enabled.swigValue());
        settingsPack.uploadRateLimit(engineSettings.uploadRateLimit);
        settingsPack.downloadRateLimit(engineSettings.downloadRateLimit);
    }

    public void saveSessionSettings() {
        if (swig() == null)
            return;
        try {
            File sessionFile = new File(TorrentFileUtils.DefaultSessionFilePath);
            FileUtils.writeByteArrayToFile(sessionFile, saveState());
        } catch (IOException ignore) {
        }
    }

    /**
     * 当前是否有任务
     */
    public boolean hasTasks() {
        return !torrentTasks.isEmpty();
    }

    /**
     * 创建一个任务
     */
    public TorrentTask buildTask(TorrentHandle torrentHandle, Torrent torrent) {
        TorrentTask torrentTask = new TorrentTask(torrent, torrentHandle, engineCallback);
        torrentTask.setMaxConnections(engineSettings.connectionsLimitPerTorrent);
        torrentTask.setMaxUploads(engineSettings.uploadsLimitPerTorrent);
        torrentTask.setSequentialDownload(torrent.isSequentialDownload());
        torrentTask.setAutoManaged(engineSettings.autoManaged);
        if (torrent.isPaused())
            torrentTask.pause();
        else
            torrentTask.resume();

        return torrentTask;
    }

    /**
     * 加载队列中任务
     */
    private void runQueueTorrentTask() {
        LoadTorrentTask loadTorrentTask = null;
        try {
            if (!loadTaskQueue.isEmpty())
                loadTorrentTask = loadTaskQueue.poll();
        } catch (Exception e) {

            return;
        }

        if (loadTorrentTask != null)
            loadTaskExecutor.execute(loadTorrentTask);
    }

    /**
     * 检查错误
     */
    private void checkError(Alert<?> alert) {
        if (engineCallback == null)
            return;

        switch (alert.type()) {
            case SESSION_ERROR: {
                SessionErrorAlert sessionErrorAlert = (SessionErrorAlert) alert;
                ErrorCode error = sessionErrorAlert.error();
                if (error.isError())
                    engineCallback.onSessionError(TorrentUtils.getErrorMsg(error));
                break;
            }
            case LISTEN_FAILED: {
                ListenFailedAlert listenFailedAlert = (ListenFailedAlert) alert;
                String errorMsg = "无法侦听（listen） %1$s:%2$s, 类型: %3$s (error: %4$s)";
                engineCallback.onSessionError(String.format(errorMsg,
                        listenFailedAlert.address(),
                        listenFailedAlert.port(),
                        listenFailedAlert.socketType(),
                        TorrentUtils.getErrorMsg(listenFailedAlert.error())));
                break;
            }
            case PORTMAP_ERROR: {
                PortmapErrorAlert portmapErrorAlert = (PortmapErrorAlert) alert;
                ErrorCode error = portmapErrorAlert.error();
                if (error.isError())
                    engineCallback.onNatError(TorrentUtils.getErrorMsg(error));
                break;
            }
        }
    }

    /**
     * 根据hash移除一个种子数据
     */
    public void removeTorrentDataByHash(String hash) {
        hashTorrentDataMap.remove(hash);
    }

    /**
     * 创建种子
     */
    private byte[] createTorrent(add_torrent_params params, torrent_info ti) {
        create_torrent ct = new create_torrent(ti);

        string_vector v = params.get_url_seeds();
        int size = (int) v.size();
        for (int i = 0; i < size; i++) {
            ct.add_url_seed(v.get(i));
        }
        string_vector trackers = params.get_trackers();
        int_vector tiers = params.get_tracker_tiers();
        size = (int) trackers.size();
        for (int i = 0; i < size; i++)
            ct.add_tracker(trackers.get(i), tiers.get(i));

        entry e = ct.generate();
        return Vectors.byte_vector2bytes(e.bencode());
    }

    /**
     * 恢复任务
     */
    public void restoreTasks(Collection<Torrent> torrents) {
        if (torrents == null)
            return;

        for (Torrent torrent : torrents) {
            if (torrent == null)
                continue;

            LoadTorrentTask loadTask = new LoadTorrentTask(torrent.getTorrentHash());
            TorrentInfo torrentInfo = new TorrentInfo(new File(torrent.getTorrentFilePath()));
            Priority[] priorities = TorrentUtils.getPriorities(torrent);
            if (priorities == null || priorities.length != torrentInfo.numFiles()) {
                if (engineCallback != null)
                    engineCallback.onRestoreSessionError(torrent.getTorrentHash());
                continue;
            }

            File resumeFile = new File(TorrentFileUtils.TaskResumeFilePath);
            File saveDir = new File(torrent.getSaveDirPath());
            loadTask.putTorrentFile(new File(torrent.getTorrentFilePath()), saveDir,
                    resumeFile, priorities);
            addTorrentsQueue.put(torrent.getTorrentHash(), torrent);
            loadTaskQueue.add(loadTask);
        }
        runQueueTorrentTask();
    }

    /**
     * 开启IP过滤
     */
    public void enableIpFilter(String path) {
        if (path == null)
            return;

        IPFilterParser parser = new IPFilterParser(path);
        parser.setOnParsedListener((ip_filter filter, boolean success) -> {
            if (success && swig() != null)
                swig().set_ip_filter(filter);
            if (engineCallback != null)
                engineCallback.onIpFilterParsed(success);
        });
        parser.parse();
    }

    /**
     * 关闭IP过滤
     */
    public void disableIpFilter() {
        swig().set_ip_filter(new ip_filter());
    }

    /**
     * 设置下载监听回调
     */
    public void setEngineCallback(TorrentEngineCallback engineCallback) {
        this.engineCallback = engineCallback;
    }

    /**
     * 设置代理配置
     */
    public void setProxy(ProxyInfo proxy) {
        if (proxy == null || proxy.getProxyType() == TorrentConfig.ProxyType.NONE)
            return;

        SettingsPack settingsPack = settings();
        settings_pack.proxy_type_t type = settings_pack.proxy_type_t.none;
        switch (proxy.getProxyType()) {
            case SOCKS4:
                type = settings_pack.proxy_type_t.socks4;
                break;
            case SOCKS5:
                type = (TextUtils.isEmpty(proxy.getIP()) ? settings_pack.proxy_type_t.socks5 :
                        settings_pack.proxy_type_t.socks5_pw);
                break;
            case HTTP:
                type = (TextUtils.isEmpty(proxy.getIP()) ? settings_pack.proxy_type_t.http :
                        settings_pack.proxy_type_t.http_pw);
                break;
        }

        settingsPack.setInteger(settings_pack.int_types.proxy_type.swigValue(), type.swigValue());
        settingsPack.setInteger(settings_pack.int_types.proxy_port.swigValue(), proxy.getPort());
        settingsPack.setString(settings_pack.string_types.proxy_hostname.swigValue(), proxy.getIP());
        settingsPack.setString(settings_pack.string_types.proxy_username.swigValue(), proxy.getAccount());
        settingsPack.setString(settings_pack.string_types.proxy_password.swigValue(), proxy.getPassword());
        settingsPack.setBoolean(settings_pack.bool_types.proxy_peer_connections.swigValue(), proxy.isPeerEnable());

        applySettingsPack(settingsPack);
    }

    /**
     * 设置一个监听端口
     */
    public void setListenPort(int port) {
        if (port == -1)
            return;

        engineSettings.port = port;
        applySettings(engineSettings);
    }

    /**
     * 设置一个随机监听端口
     */
    public void setRandomPort() {
        int randomPort = EngineSettings.MIN_PORT_NUMBER + (int) (Math.random()
                * ((EngineSettings.MAX_PORT_NUMBER - EngineSettings.MIN_PORT_NUMBER) + 1));
        setListenPort(randomPort);
    }

    /**
     * 设置每个任务的最大连接数
     */
    public void setMaxConnectionsPerTorrent(int connections) {
        engineSettings.connectionsLimitPerTorrent = connections;
        for (TorrentTask torrentTask : torrentTasks.values()) {
            if (torrentTask == null)
                continue;
            torrentTask.setMaxConnections(connections);
        }
    }

    /**
     * 设置每个任务的最大上传数
     */
    public void setMaxUploadsPerTorrent(int uploads) {
        engineSettings.uploadsLimitPerTorrent = uploads;
        for (TorrentTask torrentTask : torrentTasks.values()) {
            if (torrentTask == null)
                continue;
            torrentTask.setMaxUploads(uploads);
        }
    }

    /**
     * 设置每个任务是否为自动管理任务
     */
    public void setAutoManaged(boolean autoManaged) {
        engineSettings.autoManaged = autoManaged;
        for (TorrentTask torrentTask : torrentTasks.values())
            torrentTask.setAutoManaged(autoManaged);
    }

    /**
     * 获取一个下载任务
     */
    @Nullable
    public TorrentTask getTask(String hash) {
        return torrentTasks.get(hash);
    }

    /**
     * 获取所有任务
     */
    public Collection<TorrentTask> getTasks() {
        return torrentTasks.values();
    }

    /**
     * 根据hash获取种子数据
     */
    public byte[] getTorrentDataByHash(String hash) {
        return hashTorrentDataMap.get(hash);
    }

    /**
     * 当前任务数量
     */
    public int getTasksCount() {
        return torrentTasks.size();
    }

    /**
     * 获取IPv6 DHT节点
     */
    private static String getDHTBootstrapNodes() {
        return "dht.libtorrent.org:25401" + "," +
                "router.bittorrent.com:6881" + "," +
                "dht.transmissionbt.com:6881" + "," +
                "outer.silotis.us:6881";
    }

    /**
     * 获取所有配置
     */
    public EngineSettings getEngineSettings() {
        return engineSettings;
    }

    /**
     * 获取session参数
     */
    private SessionParams getSessionParams() {
        File sessionFile = new File(TorrentFileUtils.DefaultSessionFilePath);
        if (sessionFile.exists()) {
            try {
                byte[] data = FileUtils.readFileToByteArray(sessionFile);
                byte_vector buffer = Vectors.bytes2byte_vector(data);
                bdecode_node n = new bdecode_node();
                error_code ec = new error_code();
                int ret = bdecode_node.bdecode(buffer, n, ec);
                if (ret == 0) {
                    session_params params = libtorrent.read_session_params(n);
                    /* Prevents GC */
                    buffer.clear();

                    return new SessionParams(params);
                }
            } catch (IOException ignore) {
            }
        }
        return new SessionParams(getDefaultSettingsPack());
    }

    /**
     * 获取默认session配置
     */
    private SettingsPack getDefaultSettingsPack() {
        SettingsPack settingsPack = new SettingsPack();
        int maxQueuedDiskBytes = settingsPack.maxQueuedDiskBytes();
        int sendBufferWatermark = settingsPack.sendBufferWatermark();

        settingsPack.maxQueuedDiskBytes(maxQueuedDiskBytes / 2);
        settingsPack.sendBufferWatermark(sendBufferWatermark / 2);
        settingsPack.seedingOutgoingConnections(false);
        if (engineSettings == null) engineSettings = new EngineSettings();
        settingsToSettingsPack(engineSettings, settingsPack);

        return settingsPack;
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
     * 获取总下载量
     */
    public long getTotalDownload() {
        return stats().totalDownload();
    }

    /**
     * 获取总上传量
     */
    public long getTotalUpload() {
        return stats().totalUpload();
    }

    /**
     * 获取下载限制速度
     */
    public int getDownloadRateLimit() {
        return settings().downloadRateLimit();
    }

    /**
     * 获取上传限制速度
     */
    public int getUploadRateLimit() {
        return settings().uploadRateLimit();
    }

    /**
     * 获取监听端口
     */
    public int getListenPort() {
        return swig().listen_port();
    }

    /**
     * 获取代理配置
     */
    public ProxyInfo getProxy() {
        ProxyInfo proxy = new ProxyInfo();
        SettingsPack sp = settings();

        TorrentConfig.ProxyType type;
        String swigType = sp.getString(settings_pack.int_types.proxy_type.swigValue());

        type = TorrentConfig.ProxyType.NONE;
        if (swigType.equals(settings_pack.proxy_type_t.socks4.toString()))
            type = TorrentConfig.ProxyType.SOCKS4;
        else if (swigType.equals(settings_pack.proxy_type_t.socks5.toString()))
            type = TorrentConfig.ProxyType.SOCKS5;
        else if (swigType.equals(settings_pack.proxy_type_t.http.toString()))
            type = TorrentConfig.ProxyType.HTTP;

        proxy.setProxyType(type);
        proxy.setPort(sp.getInteger(settings_pack.int_types.proxy_port.swigValue()));
        proxy.setIP(sp.getString(settings_pack.string_types.proxy_hostname.swigValue()));
        proxy.setAccount(sp.getString(settings_pack.string_types.proxy_username.swigValue()));
        proxy.setPassword(sp.getString(settings_pack.string_types.proxy_password.swigValue()));
        proxy.setPeerEnable(sp.getBoolean(settings_pack.bool_types.proxy_peer_connections.swigValue()));

        return proxy;
    }

    /**
     * DHT是否开启
     */
    public boolean isDHTEnabled() {
        return settings().enableDht();
    }

    /**
     * PEX是否开启
     */
    public boolean isPEXEnabled() {
        return true;
    }

    /**
     * LSD是否开启
     */
    public boolean isLSDEnabled() {
        return swig() != null && settings().broadcastLSD();
    }

    private static class EngineHolder {
        static final TorrentEngine INSTANCE = new TorrentEngine();
    }

    private final class LoadTorrentTask implements Runnable {
        private String torrentId;
        private File torrentFile = null;
        private File saveDir = null;
        private File resume = null;
        private Priority[] priorities = null;
        private String uri = null;
        private boolean isMagnet;

        LoadTorrentTask(String torrentId) {
            this.torrentId = torrentId;
        }

        public void putTorrentFile(File torrentFile, File saveDir, File resume, Priority[] priorities) {
            this.torrentFile = torrentFile;
            this.saveDir = saveDir;
            this.resume = resume;
            this.priorities = priorities;
            isMagnet = false;
        }

        public void putMagnet(String uri, File saveDir) {
            this.uri = uri;
            this.saveDir = saveDir;
            isMagnet = true;
        }

        @Override
        public void run() {
            try {
                if (isMagnet) {
                    download(uri, saveDir);
                } else {
                    download(new TorrentInfo(torrentFile), saveDir, resume, priorities, null);
                }
            } catch (Exception e) {
                if (engineCallback != null)
                    engineCallback.onRestoreSessionError(torrentId);
            }
        }
    }

    private final class EngineListener implements AlertListener {
        @Override
        public int[] types() {
            return INNER_LISTENER_TYPES;
        }

        @Override
        public void alert(Alert<?> alert) {
            switch (alert.type()) {
                case ADD_TORRENT:
                    TorrentAlert<?> torrentAlert = (TorrentAlert<?>) alert;
                    TorrentHandle torrentHandle = find(torrentAlert.handle().infoHash());
                    if (torrentHandle == null)
                        break;
                    //从添加任务队列中取出任务数据
                    String hash = torrentHandle.infoHash().toHex();
                    Torrent torrent = addTorrentsQueue.get(hash);
                    if (torrent == null)
                        break;
                    //添加到下载中任务列表
                    torrentTasks.put(torrent.getTorrentHash(), buildTask(torrentHandle, torrent));
                    //回调
                    if (engineCallback != null)
                        engineCallback.onTorrentAdded(torrent.getTorrentHash());
                    runQueueTorrentTask();
                    break;
                case TORRENT_REMOVED:
                    //下载中列表移除任务
                    torrentTasks.remove(((TorrentRemovedAlert) alert).infoHash().toHex());
                    break;
                default:
                    checkError(alert);
                    break;
            }
        }
    }
}
