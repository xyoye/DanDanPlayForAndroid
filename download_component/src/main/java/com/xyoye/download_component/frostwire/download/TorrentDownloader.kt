package com.xyoye.download_component.frostwire.download

import android.os.SystemClock
import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.alerts.PieceFinishedAlert
import com.frostwire.jlibtorrent.alerts.SaveResumeDataAlert
import com.frostwire.jlibtorrent.swig.add_torrent_params
import com.frostwire.jlibtorrent.swig.entry
import com.xyoye.common_component.storage.platform.AndroidPlatform
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.getFileExtension
import com.xyoye.download_component.frostwire.TorrentEngine
import com.xyoye.download_component.frostwire.utils.DownloaderAlert
import com.xyoye.download_component.frostwire.utils.TransferState
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

/**
 * Created by xyoye on 2020/12/31.
 */

class TorrentDownloader(
    private val torrentEngine: TorrentEngine,
    private val torrentHandle: TorrentHandle
) : Downloader, Transfer {

    companion object {
        const val SAVE_RESUME_DATA_INTERVAL_MS = 10 * 1000L

        const val EXTRA_DATA_KEY = "extra_data"
    }

    private var onDownloadFinished: ((TorrentDownloader) -> Unit)? = null
    private var onDownloadRemoved: ((TorrentDownloader) -> Unit)? = null

    private val savePath = File(torrentHandle.savePath())
    private val createDate = Date(torrentHandle.status().addedTime())

    private val fileSystem = AndroidPlatform.getInstance().getFileSystem()

    private val incompleteFileList = mutableListOf<File>()
    private val piecesTracker: PiecesTracker?
    private var predominantFileExtension: String? = null
    private val downloadAlert = DownloaderAlert(this, torrentHandle)
    private val extraMap = createExtraMap()

    private var lastSaveResumeTimeMs = 0L

    init {
        val torrentInfo: TorrentInfo? = torrentHandle.torrentFile()
        if (torrentInfo != null) {
            piecesTracker = PiecesTracker(torrentInfo)
        } else {
            piecesTracker = null
        }

        torrentEngine.addListener(downloadAlert.getAlertObserver())
    }

    override fun getInfoHash(): String {
        return torrentHandle.infoHash().toString().toLowerCase(Locale.ROOT)
    }

    override fun magnetUri(): String? {
        return torrentHandle.makeMagnetUri()
    }

    override fun getConnectedPeers(): Int {
        return if (torrentHandle.isValid) torrentHandle.status().numPeers() else 0
    }

    override fun getTotalPeers(): Int {
        return if (torrentHandle.isValid) torrentHandle.status().listPeers() else 0
    }

    override fun getConnectedSeeds(): Int {
        return if (torrentHandle.isValid) torrentHandle.status().numSeeds() else 0
    }

    override fun getTotalSeeds(): Int {
        return if (torrentHandle.isValid) torrentHandle.status().listSeeds() else 0
    }

    override fun getContentSavePath(): File? {
        if (!torrentHandle.isValid)
            return null
        val torrentInfo: TorrentInfo? = torrentHandle.torrentFile()
        if (torrentInfo?.swig() != null) {
            return File(
                savePath.absolutePath,
                if (torrentInfo.numFiles() > 1)
                    torrentHandle.name()
                else
                    torrentInfo.files().filePath(0)
            )
        }
        return null
    }

    override fun isPaused(): Boolean {
        if (!torrentHandle.isValid)
            return false
        val statusPaused = torrentHandle.status().flags().and_(TorrentFlags.PAUSED).nonZero()
        return torrentHandle.isValid && (statusPaused || torrentEngine.isPaused || !torrentEngine.isRunning)
    }

    override fun isSeeding(): Boolean {
        return torrentHandle.isValid && torrentHandle.status().isSeeding
    }

    override fun isFinished(): Boolean {
        return torrentHandle.isValid && torrentHandle.status(false).isFinished
    }

    override fun pause() {
        if (!torrentHandle.isValid)
            return
        torrentHandle.unsetFlags(TorrentFlags.AUTO_MANAGED)
        torrentHandle.pause()
        saveResumeData(true)
    }

    override fun resume() {
        if (!torrentHandle.isValid)
            return
        torrentHandle.setFlags(TorrentFlags.AUTO_MANAGED)
        torrentHandle.resume()
        saveResumeData(true)
    }

    override fun getName(): String? {
        return torrentHandle.name()
    }

    override fun getDisplayName(): String {
        var count = 0
        var fileIndex = 0

        for ((index, priority) in torrentHandle.filePriorities().withIndex()) {
            if (Priority.IGNORE != priority) {
                count++
                fileIndex = index
            }
        }
        val torrentInfo: TorrentInfo? = torrentHandle.torrentFile()
        return if (count == 1 && torrentInfo != null)
            torrentInfo.files().filePath(fileIndex)
        else
            torrentHandle.name()
    }

    override fun getSavePath(): File {
        return savePath
    }

    override fun previewFile(): File? {
        //not support
        return null
    }

    override fun getSize(): Long {
        return torrentHandle.torrentFile()?.totalSize() ?: 0
    }

    override fun getCreateDate(): Date {
        return createDate
    }

    override fun getState(): TransferState {
        if (!torrentHandle.isValid) {
            return TransferState.ERROR
        }
        val torrentStats = torrentHandle.status()
        val isPaused = torrentHandle.status().flags().and_(TorrentFlags.PAUSED).nonZero()

        return when {
            !torrentEngine.isRunning -> TransferState.STOPPED
            torrentEngine.isPaused -> TransferState.PAUSED
            !torrentHandle.isValid -> TransferState.ERROR
            isPaused && torrentStats.isFinished -> TransferState.FINISHED
            isPaused && !torrentStats.isFinished -> TransferState.PAUSED
            !isPaused && torrentStats.isFinished -> TransferState.SEEDING
            else -> when (torrentStats.state()) {
                TorrentStatus.State.CHECKING_FILES -> TransferState.CHECKING
                TorrentStatus.State.DOWNLOADING_METADATA -> TransferState.DOWNLOADING_METADATA
                TorrentStatus.State.DOWNLOADING -> TransferState.DOWNLOADING
                TorrentStatus.State.FINISHED -> TransferState.FINISHED
                TorrentStatus.State.SEEDING -> TransferState.SEEDING
                TorrentStatus.State.CHECKING_RESUME_DATA -> TransferState.CHECKING
                TorrentStatus.State.UNKNOWN -> TransferState.UNKNOWN
                else -> TransferState.UNKNOWN
            }
        }
    }

    override fun getBytesReceived(): Long {
        return if (torrentHandle.isValid) torrentHandle.status().totalDone() else 0
    }

    override fun getBytesSent(): Long {
        return if (torrentHandle.isValid) torrentHandle.status().totalUpload() else 0
    }

    override fun getDownloadSpeed(): Long {
        return if (!torrentHandle.isValid || isFinished() || isPaused() || isSeeding())
            0
        else
            torrentHandle.status().downloadPayloadRate().toLong()
    }

    override fun getUploadSpeed(): Long {
        return if (!torrentHandle.isValid || (isFinished() && !isSeeding()) || isPaused())
            0
        else
            torrentHandle.status().uploadPayloadRate().toLong()
    }

    override fun getETA(): Long {
        if (!torrentHandle.isValid)
            return 0

        val torrentInfo = torrentHandle.torrentFile() ?: return 0
        val torrentStatus = torrentHandle.status()

        val surplus = max(0, torrentInfo.totalSize() - torrentStatus.totalDone())
        var rate = torrentStatus.downloadPayloadRate()

        if (rate <= 0)
            rate = -1

        return surplus / rate
    }

    override fun getProgress(): Int {
        if (!torrentHandle.isValid)
            return 0
        val torrentStatus = torrentHandle.status()
        val progress = torrentStatus.progress()

        val state = torrentStatus.state()
        if (progress == 1f && state != TorrentStatus.State.CHECKING_FILES) {
            return 100
        }

        val progressInt = (progress * 100).toInt()
        if (progressInt > 0 && state != TorrentStatus.State.CHECKING_FILES) {
            return min(progressInt, 100)
        }

        return 0
    }

    override fun isDownloading(): Boolean {
        return getDownloadSpeed() > 0
    }

    override fun isComplete(): Boolean {
        return getProgress() == 100
    }

    override fun getItems(): MutableList<TransferItem> {
        val items = mutableListOf<TransferItem>()
        if (torrentHandle.isValid) {
            val torrentInfo: TorrentInfo? = torrentHandle.torrentFile()
            if (torrentInfo?.isValid == true) {
                val fileStorage = torrentInfo.files()
                val fileCount = torrentInfo.numFiles()
                for (index in 0 until fileCount) {
                    items.add(
                        TorrentItemDownloader(
                            torrentHandle,
                            index,
                            fileStorage.filePath(index),
                            fileStorage.fileSize(index),
                            piecesTracker
                        )
                    )
                }
                if (piecesTracker != null) {
                    val piecesNum = torrentInfo.numPieces()
                    for (index in 0 until piecesNum) {
                        if (torrentHandle.havePiece(index)) {
                            piecesTracker.setComplete(index, true)
                        }
                    }
                }
            }
        }
        return items
    }

    override fun remove(deleteData: Boolean) {
        incompleteFileList.clear()

        if (torrentHandle.isValid) {
            if (deleteData) {
                incompleteFileList.addAll(getIncompleteFiles())
                torrentEngine.remove(torrentHandle, SessionHandle.DELETE_FILES)
            } else {
                torrentEngine.remove(torrentHandle)
            }
        }
        val infoHash = torrentHandle.infoHash().toString().toLowerCase(Locale.ROOT)

        val resumeTorrentFile = PathHelper.resumeTorrentFile(infoHash)
        val resumeDataFile = PathHelper.resumeDataFile(infoHash)
        fileSystem.delete(resumeTorrentFile)
        fileSystem.delete(resumeDataFile)
    }

    override fun getPredominantFileExtension(): String? {
        if (predominantFileExtension == null) {
            val torrentInfo = torrentHandle.torrentFile() ?: return null
            val fileStorage = torrentInfo.files()
            val extensionByteSums = HashMap<String, Long>()
            val fileCount = fileStorage.numFiles()

            for (index in 0 until fileCount) {
                val path = fileStorage.filePath(index)
                val extension = getFileExtension(path)
                if (extension.isEmpty())
                    continue
                if (extensionByteSums.containsKey(extension)) {
                    val newSize = fileStorage.fileSize(index) + (extensionByteSums[extension] ?: 0)
                    extensionByteSums[extension] = newSize
                } else {
                    extensionByteSums[extension] = fileStorage.fileSize(index)
                }
            }
            var extensionCandidate: String? = null
            for (extension in extensionByteSums.keys) {
                if (extensionCandidate == null) {
                    extensionCandidate = extension
                } else {
                    val currentSize = extensionByteSums[extension] ?: 0
                    val lastMaxSize = extensionByteSums[extensionCandidate] ?: 0
                    if (currentSize > lastMaxSize) {
                        extensionCandidate = extension
                    }
                }
            }
            predominantFileExtension = extensionCandidate
        }
        return predominantFileExtension
    }

    private fun getIncompleteFiles(): MutableList<File> {
        val incompleteFileList = mutableListOf<File>()
        if (!torrentHandle.isValid)
            return incompleteFileList

        val torrentInfo: TorrentInfo = torrentHandle.torrentFile() ?: return incompleteFileList

        val progresses =
            torrentHandle.fileProgress(TorrentHandle.FileProgressFlags.PIECE_GRANULARITY)
        val fileStorage = torrentInfo.files()
        val saveDirPath = savePath.absolutePath
        val createTime = createDate.time

        for ((index, progress) in progresses.withIndex()) {
            val filePath = fileStorage.filePath(index)
            val fileSize = fileStorage.fileSize(index)
            if (progress < fileSize) {
                val incompleteFile = File(saveDirPath, filePath)
                if (!incompleteFile.exists()) {
                    continue
                }
                if (incompleteFile.lastModified() >= createTime) {
                    incompleteFileList.add(incompleteFile)
                }
            }
        }
        return incompleteFileList
    }

    private fun createExtraMap(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val infoHash = torrentHandle.infoHash().toString()
        val resumeFile = PathHelper.resumeDataFile(infoHash)
        if (!resumeFile.exists())
            return map

        val data = fileSystem.read(resumeFile) ?: return map
        var entryMap = entry.bdecode(Vectors.bytes2byte_vector(data)).dict()
        if (entryMap.has_key(EXTRA_DATA_KEY)) {
            entryMap = entryMap.get(EXTRA_DATA_KEY).dict()
            val keys = entryMap.keys()
            for (index in 0 until (keys.size().toInt())) {
                val mapData = entryMap.get(keys[index])
                if (mapData.type() == entry.data_type.string_t) {
                    map[keys[index]] = mapData.string()
                }
            }
        }

        return map
    }

    fun saveResumeData(force: Boolean) {
        val now = SystemClock.uptimeMillis()
        if (force || (now - lastSaveResumeTimeMs) >= SAVE_RESUME_DATA_INTERVAL_MS) {
            lastSaveResumeTimeMs = now

            if (torrentHandle.isValid) {
                try {
                    torrentHandle.saveResumeData(TorrentHandle.SAVE_INFO_DICT)
                } catch (t: Throwable) {
                    DDLog.w("Error triggering resume data", t)
                }
            }
        }
    }

    fun torrentFinished() {
        saveResumeData(true)
        onDownloadFinished?.invoke(this)
    }

    fun torrentRemoved() {
        torrentEngine.removeListener(downloadAlert.getAlertObserver())
        for (incompleteFile in incompleteFileList){
            if (incompleteFile.exists()){
                if (!fileSystem.delete(incompleteFile)){
                    DDLog.i("Can't delete file $incompleteFile")
                }
            }
        }
        // TODO: 2021/1/5 删除保存的文件夹
        onDownloadRemoved?.invoke(this)
    }

    fun torrentChecked() {
        if (!torrentHandle.isValid) {
            getItems()
        }
    }

    fun serializeResumeData(alert: SaveResumeDataAlert) {
        if (torrentHandle.isValid) {
            val infoHash = torrentHandle.infoHash().toString()
            val resumeFile = PathHelper.resumeDataFile(infoHash)
            val entry = add_torrent_params.write_resume_data(alert.swig().params)
            entry.dict().set(EXTRA_DATA_KEY, Entry.fromMap(extraMap).swig())
            fileSystem.write(resumeFile, Vectors.byte_vector2bytes(entry.bencode()))
        }
    }

    fun pieceFinished(alert: PieceFinishedAlert) {
        piecesTracker?.setComplete(alert.pieceIndex(), true)
        saveResumeData(false)
    }

    infix fun onDownloadFinished(block: (TorrentDownloader) -> Unit) {
        onDownloadFinished = block
    }

    infix fun onDownloadRemoved(block: (TorrentDownloader) -> Unit) {
        onDownloadRemoved = block
    }
}