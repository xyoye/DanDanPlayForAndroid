package com.xyoye.download_component.frostwire.download

import com.frostwire.jlibtorrent.PiecesTracker
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import java.io.File

/**
 * Created by xyoye on 2021/1/1.
 */

class TorrentItemDownloader(
    private val torrentHandle: TorrentHandle,
    private val index: Int,
    filePath: String,
    private val fileSize: Long,
    private val piecesTracker: PiecesTracker?
) : TransferItem {

    private val file = File(torrentHandle.savePath(), filePath)
    private val name = file.name


    override fun getName(): String {
        return name
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun getFile(): File {
        return file
    }

    override fun getSize(): Long {
        return fileSize
    }

    override fun isSkipped(): Boolean {
        return torrentHandle.filePriority(index) == Priority.IGNORE
    }

    override fun getDownloaded(): Long {
        if (torrentHandle.isValid) {
            val progresses =
                torrentHandle.fileProgress(TorrentHandle.FileProgressFlags.PIECE_GRANULARITY)
            return progresses[index]
        }
        return 0
    }

    override fun getProgress(): Int {
        if (!torrentHandle.isValid || fileSize == 0L) {
            return 0
        }

        val downloaded = getDownloaded()
        return if (downloaded == fileSize)
            100
        else
            (getDownloaded().toFloat() * 100 / fileSize.toFloat()).toInt()
    }

    override fun isComplete(): Boolean {
        return getDownloaded() == fileSize
    }

    fun getSequentialDownloaded(): Long {
        return piecesTracker?.getSequentialDownloadedBytes(index) ?: 0
    }

}