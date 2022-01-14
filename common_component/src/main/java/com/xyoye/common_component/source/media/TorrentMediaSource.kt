package com.xyoye.common_component.source.media

import com.xunlei.downloadlib.parameter.TorrentFileInfo
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/14.
 */

class TorrentMediaSource(
    private val index: Int,
    private val videoSources: List<TorrentFileInfo>,
    private val playUrl: String,
    private val torrentPath: String,
    private var currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?
) : BaseVideoSource(index, videoSources) {

    override fun getVideoUrl(): String {
        return playUrl
    }

    override fun getVideoTitle(): String {
        return videoSources[index].mFileName
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)?.mFileName ?: ""
    }

    override fun getMediaType(): MediaType {
        return MediaType.MAGNET_LINK
    }

    override fun getDanmuPath(): String? {
        return danmuPath
    }

    override fun setDanmuPath(path: String) {
        danmuPath = path
    }

    override fun getEpisodeId(): Int {
        return episodeId
    }

    override fun setEpisodeId(id: Int) {
        episodeId = id
    }

    override fun getSubtitlePath(): String? {
        return subtitlePath
    }

    override fun setSubtitlePath(path: String) {
        subtitlePath = path
    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }

    override fun getUniqueKey(): String {
        return getFileNameNoExtension(getTorrentPath())
    }

    override suspend fun indexSource(index: Int): BaseVideoSource? {
        val source = VideoSourceFactory.Builder()
            .setRootPath(torrentPath)
            .setIndex(index)
            .create(getMediaType())
            ?: return null
        return source as TorrentMediaSource
    }

    fun getPlayTaskId(): Long {
        return ThunderManager.getInstance().getTaskId(torrentPath)
    }

    fun getTorrentPath(): String {
        return torrentPath
    }

    fun getTorrentIndex(): Int {
        return index
    }
}