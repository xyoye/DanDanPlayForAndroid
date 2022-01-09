package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.VideoSource
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/21.
 */

class HistoryMediaSource(
    private val history: PlayHistoryEntity
) : VideoSource, ExtraSource {
    private var danmuPath = history.danmuPath
    private var episodeId = history.episodeId
    private var subtitlePath = history.subtitlePath

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
        return history.url
    }

    override fun getVideoTitle(): String {
        return history.videoName
    }

    override fun getCurrentPosition(): Long {
        return history.videoPosition
    }

    override fun getMediaType(): MediaType {
        return history.mediaType
    }

    override fun getHttpHeader(): Map<String, String>? {
        return history.httpHeader?.let {
            JsonHelper.parseJsonMap(it)
        }
    }

    override fun getUniqueKey(): String {
        return history.uniqueKey
    }
}