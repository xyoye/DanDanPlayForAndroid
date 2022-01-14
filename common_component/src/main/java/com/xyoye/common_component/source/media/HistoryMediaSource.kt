package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/21.
 */

class HistoryMediaSource(
    private val videoSources: List<PlayHistoryEntity>,
    private val index: Int
) : BaseVideoSource(index, emptyList<Any>()) {
    private var danmuPath = videoSources[index].danmuPath
    private var episodeId = videoSources[index].episodeId
    private var subtitlePath = videoSources[index].subtitlePath

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

    override fun getVideoUrl(): String {
        return videoSources[index].url
    }

    override fun getVideoTitle(): String {
        return videoSources[index].videoName
    }

    override fun getCurrentPosition(): Long {
        return videoSources[index].videoPosition
    }

    override fun getMediaType(): MediaType {
        return videoSources[index].mediaType
    }

    override fun getHttpHeader(): Map<String, String>? {
        return videoSources[index].httpHeader?.let {
            JsonHelper.parseJsonMap(it)
        }
    }

    override fun getUniqueKey(): String {
        return videoSources[index].uniqueKey
    }

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)?.videoName ?: ""
    }

    override suspend fun indexSource(index: Int): BaseVideoSource? {
        val source = VideoSourceFactory.Builder()
            .setVideoSources(videoSources)
            .setIndex(index)
            .createHistory()
            ?: return null
        return source as HistoryMediaSource
    }
}