package com.xyoye.download_component.frostwire.utils

import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentInfo
import com.xyoye.common_component.utils.DDLog
import com.xyoye.download_component.frostwire.TorrentEngine
import java.io.File

/**
 * Created by xyoye on 2021/1/1.
 */

class RestoreDownloadTask(
    private val engine: TorrentEngine,
    private val torrentFile: File,
    private val saveDirectory: File?,
    private val priorities: Array<Priority>?,
    private val resumeFile: File?
) : Runnable {

    override fun run() {
        try {
            engine.download(TorrentInfo(torrentFile), saveDirectory, resumeFile, priorities, null)
        } catch (throwable: Throwable) {
            DDLog.e(
                "Unable to restore download from previous session. (" + torrentFile.absolutePath + ")",
                throwable
            )
        }

    }

}