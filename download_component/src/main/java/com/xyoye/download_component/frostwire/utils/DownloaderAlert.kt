package com.xyoye.download_component.frostwire.utils

import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.alerts.*
import com.xyoye.common_component.utils.DDLog
import com.xyoye.download_component.frostwire.download.TorrentDownloader

/**
 * Created by xyoye on 2020/12/31.
 */

class DownloaderAlert(
    private val downloader: TorrentDownloader,
    private val torrentHandle: TorrentHandle
) {
    private val acceptAlertType = intArrayOf(
        AlertType.TORRENT_FINISHED.swig(),
        AlertType.TORRENT_REMOVED.swig(),
        AlertType.TORRENT_CHECKED.swig(),
        AlertType.SAVE_RESUME_DATA.swig(),
        AlertType.PIECE_FINISHED.swig(),
        AlertType.STORAGE_MOVED.swig()
    )

    private val alertObserver = object : AlertListener {

        override fun types() = acceptAlertType

        override fun alert(alert: Alert<*>?) {
            if (alert == null || alert !is TorrentAlert<*>)
                return

            if (!alert.handle().swig().op_eq(torrentHandle.swig()))
                return

            DDLog.i(alert.type().name)

            when (alert.type()) {
                AlertType.TORRENT_FINISHED -> {
                    downloader.torrentFinished()
                }
                AlertType.TORRENT_REMOVED -> {
                    downloader.torrentRemoved()
                }
                AlertType.TORRENT_CHECKED -> {
                    downloader.torrentChecked()
                }
                AlertType.SAVE_RESUME_DATA -> {
                    downloader.serializeResumeData(alert as SaveResumeDataAlert)
                }
                AlertType.PIECE_FINISHED -> {
                    downloader.pieceFinished(alert as PieceFinishedAlert)
                }
                AlertType.STORAGE_MOVED -> {
                    downloader.saveResumeData(true)
                }
                else -> {
                }
            }
        }
    }

    fun getAlertObserver() = alertObserver
}