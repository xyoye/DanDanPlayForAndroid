package com.xyoye.download_component.frostwire

import androidx.lifecycle.MutableLiveData
import com.frostwire.jlibtorrent.TorrentInfo
import com.xyoye.common_component.storage.platform.AndroidPlatform
import com.xyoye.download_component.frostwire.download.TorrentDownloader
import com.xyoye.download_component.frostwire.io.LibTorrentStorage
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by xyoye on 2021/1/2.
 */

class TransferManager private constructor() {
    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = TransferManager()
    }

    private val fileSystem = AndroidPlatform.getInstance().getFileSystem()
    private val downloadMap = ConcurrentHashMap<String, TorrentDownloader>()
    private val downloadLiveData = MutableLiveData<MutableList<TorrentDownloader>>()
    private val downloadExecutor =
        ThreadPoolExecutor(10, 30, 200, TimeUnit.MILLISECONDS, ArrayBlockingQueue(20))

    private val isUpdateDownload = AtomicBoolean(false)
    private val isDownloadRunning = AtomicBoolean(true)

    init {
        LibTorrentStorage.adaptAndroidFileSystem()

        TorrentEngine.getInstance().apply {
            onDownloadAdded { engine, downloader ->
                observerDownloadChange(downloader)
                downloadMap[downloader.getInfoHash()] = downloader
                updateDownloadLiveData()
            }

            onDownloadUpdate { engine, downloader ->
                downloadMap[downloader.getInfoHash()] = downloader
                updateDownloadLiveData()
            }

            start()

            downloadExecutor.execute {
                restoreDownloads()
            }
        }

        startUpdateDownloadTask()
    }

    fun start() {
        isUpdateDownload.set(true)
    }

    fun stop() {
        isUpdateDownload.set(false)
    }

    fun shutDownload() {
        stop()
        isDownloadRunning.set(false)
        downloadExecutor.shutdown()
        TorrentEngine.getInstance().stop()
    }

    fun observerDownload() = downloadLiveData

    fun download(torrentPath: String, selections: ByteArray?) {
        val file = File(torrentPath)
        if (file.exists() && file.canRead()) {
            val torrentInfo = TorrentInfo(file)
            val booleanSelections: BooleanArray
            if (selections == null || selections.isEmpty()) {
                booleanSelections = BooleanArray(torrentInfo.numFiles()) { true }
            } else {
                booleanSelections = BooleanArray(selections.size)
                for ((index, selection) in selections.withIndex()) {
                    booleanSelections[index] = selection == 1.toByte()
                }
            }
            TorrentEngine.getInstance().download(torrentInfo, booleanSelections)
        }
    }

    fun removeDownload(downloader: TorrentDownloader, deleteData: Boolean) {
        downloadMap.remove(downloader.getInfoHash())
        updateDownloadLiveData()
        downloader.remove(deleteData)
    }

    fun removeFinished() {
        for (downloader in downloadMap.values) {
            if (downloader.isComplete()) {
                downloadMap.remove(downloader.getInfoHash())
                updateDownloadLiveData()
                downloader.remove(false)
            }
        }
    }

    private fun updateDownloadLiveData() {
        if (downloadMap.size > 0) {
            downloadLiveData.postValue(downloadMap.values.toMutableList())
        } else {
            downloadLiveData.postValue(arrayListOf())
        }
    }

    private fun startUpdateDownloadTask() {
        downloadExecutor.execute {
            while (isDownloadRunning.get()) {
                if (isUpdateDownload.get()) {
                    updateDownloadLiveData()
                    try {
                        Thread.sleep(1000)
                    } catch (t: Throwable) {
                        // ignore
                    }
                }
            }
        }
    }

    private fun observerDownloadChange(downloader: TorrentDownloader) {
        downloader.apply {
            onDownloadRemoved {
                downloadMap.remove(it.getInfoHash())
            }

            onDownloadFinished {
                val saveDirectory = it.getContentSavePath()
                if (saveDirectory != null) {
                    fileSystem.scan(saveDirectory)
                }
            }
        }
    }
}