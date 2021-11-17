package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.MediaSource
import com.xyoye.common_component.source.helper.LocalMediaSourceHelper
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/14.
 */

class LocalMediaSource private constructor(
    private val index: Int,
    private val videoSources: List<VideoEntity>,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?
) : MediaSource(index, videoSources) {

    companion object {

        suspend fun build(index: Int, videoSources: List<VideoEntity>?): LocalMediaSource? {
            val video = videoSources?.getOrNull(index) ?: return null

            val (episodeId, danmuPath) = LocalMediaSourceHelper.getVideoDanmu(video)
            val subtitlePath = LocalMediaSourceHelper.getVideoSubtitle(video)
            val position = LocalMediaSourceHelper.getHistoryPosition(video)
            return LocalMediaSource(
                index,
                videoSources,
                position,
                danmuPath,
                episodeId,
                subtitlePath
            )
        }
    }

    override fun getVideoUrl(): String {
        return videoSources[index].filePath
    }

    override fun getVideoTitle(): String {
        return getFileName(getVideoUrl())
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun getDanmuPath(): String? {
        return danmuPath
    }

    override fun setDanmuPath(path: String) {
        danmuPath = path
        videoSources[index].danmuPath = path
    }

    override fun getEpisodeId(): Int {
        return episodeId
    }

    override fun setEpisodeId(id: Int) {
        episodeId = id
        videoSources[index].danmuId = id
    }

    override fun getSubtitlePath(): String? {
        return subtitlePath
    }

    override fun setSubtitlePath(path: String) {
        subtitlePath = path
        videoSources[index].subtitlePath = path
    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }

    override fun getMediaType(): MediaType {
        return MediaType.LOCAL_STORAGE
    }

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)?.filePath?.let {
            getFileName(it)
        } ?: ""
    }

    override suspend fun indexSource(index: Int): MediaSource? {
        if (index in videoSources.indices)
            return build(index, videoSources)
        return null
    }
}