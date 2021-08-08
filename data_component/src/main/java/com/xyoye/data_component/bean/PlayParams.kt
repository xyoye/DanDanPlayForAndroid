package com.xyoye.data_component.bean

import android.os.Parcelable
import com.xyoye.data_component.enums.MediaType
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/10/28.
 */

@Parcelize
data class PlayParams(
    var videoPath: String,
    var videoTitle: String?,
    var danmuPath: String?,
    var subtitlePath: String?,
    var currentPosition: Long,
    var episodeId: Int,
    var mediaType: MediaType,
    var extra: HashMap<String, String>? = null
) : Parcelable {

    fun setTorrentPath(torrentPath: String) {
        getExtraMap()["torrent_path"] = torrentPath
    }

    fun setTorrentFileIndex(fileIndex: Int) {
        getExtraMap()["torrent_file_index"] = fileIndex.toString()
    }

    fun setTorrentTitle(torrentTitle: String?) {
        getExtraMap()["torrent_title"] = torrentTitle ?: ""
    }

    fun setHttpHeader(headerJson: String?) {
        getExtraMap()["http_header"] = headerJson ?: ""
    }

    fun setPlayTaskId(taskId: Long) {
        getExtraMap()["torrent_task_id"] = taskId.toString()
    }

    fun getHttpHeaderJson(): String {
        return getExtraMap()["http_header"] ?: ""
    }

    fun getPlayTaskId() : Long{
        return getExtraMap()["torrent_task_id"]?.toLongOrNull() ?: -1L
    }

    private fun getExtraMap(): HashMap<String, String> {
        if (extra == null)
            extra = hashMapOf()

        return extra!!
    }
}