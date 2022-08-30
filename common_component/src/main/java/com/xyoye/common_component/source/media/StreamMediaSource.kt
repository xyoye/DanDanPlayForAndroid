package com.xyoye.common_component.source.media

import com.xyoye.common_component.extension.decodeUrl
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/21.
 */

class StreamMediaSource(
    private val index: Int,
    private val videoSources: List<String>,
    private val header: Map<String, String>? = null,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?,
    private val uniqueKey: String,
    private val mediaType: MediaType,
) : BaseVideoSource(index, videoSources) {

    override fun getVideoUrl(): String {
        return videoSources[index]
    }

    override fun getVideoTitle(): String {
        return getFileName(videoSources[index].decodeUrl())
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun getMediaType(): MediaType {
        return mediaType
    }

    override fun getHttpHeader(): Map<String, String>? {
        return header
    }

    override fun getUniqueKey(): String {
        return uniqueKey
    }

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)
            ?.let { getFileName(it) }
            ?: ""
    }

    override suspend fun indexSource(index: Int): BaseVideoSource? {
        val source = VideoSourceFactory.Builder()
            .setVideoSources(videoSources)
            .setIndex(index)
            .setHttpHeaders(header ?: emptyMap())
            .create(getMediaType())
            ?: return null
        return source as StreamMediaSource
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
}