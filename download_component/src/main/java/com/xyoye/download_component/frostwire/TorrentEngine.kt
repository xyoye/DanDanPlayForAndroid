package com.xyoye.download_component.frostwire

import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.swig.libtorrent
import com.frostwire.jlibtorrent.swig.settings_pack
import com.xyoye.common_component.config.DownloadConfig
import com.xyoye.common_component.storage.file_system.FileFilter
import com.xyoye.common_component.storage.platform.AndroidPlatform
import com.xyoye.common_component.utils.*
import com.xyoye.download_component.frostwire.download.TorrentDownloader
import com.xyoye.download_component.frostwire.utils.EngineAlert
import com.xyoye.download_component.frostwire.utils.EngineUtils
import com.xyoye.download_component.frostwire.utils.RestoreDownloadTask
import java.io.File
import java.util.*
import kotlin.random.Random

/**
 * Created by xyoye on 2020/12/29.
 */

class TorrentEngine private constructor() : SessionManager() {

    private var onEngineStarted: ((engine: TorrentEngine) -> Unit)? = null
    private var onEngineStopped: ((engine: TorrentEngine) -> Unit)? = null
    private var onDownloadAdded: ((engine: TorrentEngine, downloader: TorrentDownloader) -> Unit)? =
        null
    private var onDownloadUpdate: ((engine: TorrentEngine, downloader: TorrentDownloader) -> Unit)? =
        null

    private val alertListener = EngineAlert(this).getAlertObserver()
    private val fileSystem = AndroidPlatform.getInstance().getFileSystem()

    private val restoreDownloadQueue = LinkedList<RestoreDownloadTask>()

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = TorrentEngine()
    }

    override fun onBeforeStart() {
        addListener(alertListener)
    }

    override fun start(params: SessionParams?) {
        val sessionParams = EngineUtils.loadSettings()
        val settingsPack = sessionParams.settings().swig()
        settingsPack.apply {
            //监听端口
            val port = 37000 + Random.nextInt(20000)
            set_str(
                settings_pack.string_types.listen_interfaces.swigValue(),
                "0.0.0.0:$port,[::]:$port"
            )
            //监听端口重试范围
            set_int(settings_pack.int_types.max_retry_port_bind.swigValue(), 10)
            //初始dht节点
            set_str(
                settings_pack.string_types.dht_bootstrap_nodes.swigValue(),
                EngineUtils.getDHTBootstrapNodes()
            )
            set_int(settings_pack.int_types.active_limit.swigValue(), 2000)
            set_int(settings_pack.int_types.stop_tracker_timeout.swigValue(), 0)
            //消息队列大小
            set_int(settings_pack.int_types.alert_queue_size.swigValue(), 5000)
            //是否开启dht网络
            set_bool(settings_pack.bool_types.enable_dht.swigValue(), DownloadConfig.isDhtEnable())
            //识别码
            val fingerPrintData = EngineUtils.getFingerPrintData()
            set_str(
                settings_pack.string_types.peer_fingerprint.swigValue(),
                libtorrent.generate_fingerprint(
                    "DanDanPlay-Android",
                    fingerPrintData[0],
                    fingerPrintData[1],
                    fingerPrintData[2],
                    fingerPrintData[3]
                )
            )
            //UserAgent
            set_str(
                settings_pack.string_types.user_agent.swigValue(),
                "DanDanPlay-Android ${AppUtils.getVersionName()}/libtorrent ${libtorrent.version()}"
            )
        }
        super.start(sessionParams)
    }

    override fun onAfterStart() {
        onEngineStarted?.invoke(this)
    }

    override fun onBeforeStop() {
        removeListener(alertListener)
        if (swig() != null) {
            EngineUtils.saveSettings(saveState())
        }
    }

    override fun onAfterStop() {
        onEngineStopped?.invoke(this)
    }

    override fun saveState(): ByteArray {
        val session = swig()
        if (session != null) {
            return EngineUtils.buildSettings(session)
        }
        return byteArrayOf()
    }

    override fun onApplySettings(sp: SettingsPack?) {
        if (isSessionExist()) {
            EngineUtils.saveSettings(saveState())
        }
    }

    private fun saveResumeTorrent(torrentInfo: TorrentInfo) {
        val entryData = torrentInfo.toEntry().swig()
        val torrentData = Vectors.byte_vector2bytes(entryData.bencode())
        val infoHash = torrentInfo.infoHash().toString().toLowerCase(Locale.ROOT)
        val resumeTorrentFile = PathHelper.resumeTorrentFile(infoHash)
        fileSystem.write(resumeTorrentFile, torrentData)
    }

    private fun newDownload(
        torrentInfo: TorrentInfo,
        saveDirectory: File,
        priorities: Array<Priority>,
        peers: MutableList<TcpEndpoint>?
    ) {
        val torrentHandle: TorrentHandle? = find(torrentInfo.infoHash())
        if (torrentHandle != null) {
            //task already exists
            if (torrentInfo.numFiles() != priorities.size) {
                throw IllegalArgumentException("The priorities length should be equals to the number of files")
            }
            torrentHandle.prioritizeFiles(priorities)
            onTaskUpdate(torrentHandle)
            torrentHandle.resume()
        } else {
            //new task
            download(torrentInfo, saveDirectory, null, priorities, peers)
            val newTorrentHandle: TorrentHandle? = find(torrentInfo.infoHash())
            if (newTorrentHandle != null) {
                onTaskUpdate(newTorrentHandle)
            }
        }
    }

    private fun isSessionExist() = swig() != null

    private fun onTaskUpdate(torrentHandle: TorrentHandle) {
        val torrentDownloader = TorrentDownloader(this, torrentHandle)
        onDownloadUpdate?.invoke(this, torrentDownloader)
    }

    fun restoreDownloads() {
        if (!isSessionExist())
            return

        val resumeTorrentDirectory = PathHelper.getDownloadResumeDirectory()
        val resumeTorrents = fileSystem.listFiles(resumeTorrentDirectory, object : FileFilter {
            override fun accept(file: File): Boolean {
                return getFileExtension(file) == "torrent"
            }

            override fun file(file: File) {

            }
        }) ?: return

        for (resumeTorrentFile in resumeTorrents) {
            val infoHash = getFileNameNoExtension(resumeTorrentFile)
            if (infoHash.isEmpty()) continue
            val resumeDataFile = PathHelper.resumeDataFile(infoHash)
            restoreDownloadQueue.add(
                RestoreDownloadTask(
                    this,
                    resumeTorrentFile,
                    null,
                    null,
                    resumeDataFile
                )
            )
        }
        runNextRestoreDownloadTask()
    }

    fun download(
        torrentInfo: TorrentInfo,
        selections: BooleanArray,
        peers: MutableList<TcpEndpoint>? = null
    ) {
        if (!isSessionExist()) {
            return
        }

        val downloadDir = PathHelper.getDownloadDirectory()

        val saveDirectory = EngineUtils.checkSaveDirectory(downloadDir) ?: return
        var priorities = Array(torrentInfo.numFiles()) { Priority.IGNORE }

        if (selections.isEmpty()) {
            Arrays.fill(priorities, Priority.NORMAL)
        } else {
            val defaultPriorities = find(torrentInfo.infoHash())?.filePriorities()
            if (defaultPriorities != null) {
                priorities = defaultPriorities
            }

            for ((index, selection) in selections.withIndex()) {
                if (selection && index < priorities.size && priorities[index] == Priority.IGNORE) {
                    priorities[index] = Priority.NORMAL
                }
            }
        }

        newDownload(torrentInfo, saveDirectory, priorities, peers)

        saveResumeTorrent(torrentInfo)
    }

    fun setDownloadAdded(infoHash: Sha1Hash) {
        try {
            val torrentHandle = find(infoHash)
            if (torrentHandle != null) {
                val downloader = TorrentDownloader(this, torrentHandle)
                onDownloadAdded?.invoke(this, downloader)
            } else {
                DDLog.i("torrent was not successfully added")
            }
        } catch (t: Throwable) {
            DDLog.e("Unable to create and/or notify the new download", t)
        }
    }

    fun runNextRestoreDownloadTask() {
        var restoreDownloadTask: RestoreDownloadTask? = null
        try {
            if (!restoreDownloadQueue.isEmpty()) {
                restoreDownloadTask = restoreDownloadQueue.poll()
            }
        } catch (t: Throwable) {
            // on Android, LinkedList's .poll() implementation throws a NoSuchElementException
        }
        restoreDownloadTask?.run()
    }

    infix fun onDownloadAdded(block: ((engine: TorrentEngine, downloader: TorrentDownloader) -> Unit)) {
        this.onDownloadAdded = block
    }

    infix fun onDownloadUpdate(block: ((engine: TorrentEngine, downloader: TorrentDownloader) -> Unit)) {
        this.onDownloadUpdate = block
    }

    infix fun onEngineStart(block: ((engine: TorrentEngine) -> Unit)) {
        this.onEngineStarted = block
    }

    infix fun onEngineStopped(block: ((engine: TorrentEngine) -> Unit)) {
        this.onEngineStopped = block
    }
}