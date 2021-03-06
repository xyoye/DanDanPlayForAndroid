package com.xyoye.download_component.frostwire.utils

import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.alerts.*
import com.xyoye.common_component.utils.DDLog
import com.xyoye.download_component.frostwire.TorrentEngine

/**
 * Created by xyoye on 2020/12/29.
 */

class EngineAlert(private val engine: TorrentEngine) {
    private val mTag = EngineAlert::class.java.simpleName

    private val acceptAlertType = intArrayOf(
        AlertType.ADD_TORRENT.swig(),
        AlertType.LISTEN_SUCCEEDED.swig(),
        AlertType.LISTEN_FAILED.swig(),
        AlertType.EXTERNAL_IP.swig(),
        AlertType.FASTRESUME_REJECTED.swig(),
        AlertType.DHT_BOOTSTRAP.swig(),
        AlertType.TORRENT_LOG.swig(),
        AlertType.PEER_LOG.swig(),
        AlertType.LOG.swig()
    )

    private val alertObserver = object : AlertListener {

        override fun types() = acceptAlertType

        override fun alert(alert: Alert<*>?) {
            val alertType = alert?.type() ?: return

            DDLog.i(alert.type().name)

            when (alertType) {
                AlertType.ADD_TORRENT -> {
                    val torrentAlert = alert as TorrentAlert<*>
                    val infoHash = torrentAlert.handle().infoHash()
                    engine.setDownloadAdded(infoHash)
                    engine.runNextRestoreDownloadTask()
                }
                AlertType.LISTEN_SUCCEEDED -> {
                    (alert as ListenSucceededAlert).apply {
                        val logText = "Listen succeeded on endpoint:" +
                                " ${address()}:${port()}" +
                                " type: ${socketType()}"
                        DDLog.i(mTag, logText)
                    }
                }
                AlertType.LISTEN_FAILED -> {
                    (alert as ListenFailedAlert).apply {
                        val logText = "Listen failed on endpoint:" +
                                " ${address()}:${port()}" +
                                " type: ${socketType()}," +
                                " (error: ${alert.error().message()})"
                        DDLog.i(mTag, logText)
                    }
                }
                AlertType.EXTERNAL_IP -> {
                    (alert as ExternalIpAlert).apply {
                        val logText = "External IP:${externalAddress()}"
                        DDLog.i(mTag, logText)
                    }
                }
                AlertType.FASTRESUME_REJECTED -> {
                    (alert as FastresumeRejectedAlert).apply {
                        val logText = "Failed to load fast resume data, " +
                                "path: ${filePath()}, " +
                                "operation: ${operation()}, " +
                                "error: ${error().message()}"
                        DDLog.w(mTag, logText)
                    }
                }
                AlertType.DHT_BOOTSTRAP -> {

                }
                AlertType.TORRENT_LOG,
                AlertType.PEER_LOG,
                AlertType.LOG -> {
                    DDLog.i(mTag, "log: $alert")
                }
                else -> {
                    DDLog.i(mTag, "log: $alert")
                }
            }
        }
    }

    fun getAlertObserver() = alertObserver
}