package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.helper.RemoteMediaSourceHelper
import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.GroupSource
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.RemoteHelper
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/21.
 */

class RemoteMediaSource private constructor(
    private val index: Int,
    private val videoSources: List<RemoteVideoData>,
    private val playUrl: String,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?
) : GroupVideoSource(index, videoSources), ExtraSource {

    companion object {

        suspend fun build(
            index: Int,
            videoSources: List<RemoteVideoData>
        ): RemoteMediaSource? {
            val videoData = videoSources.getOrNull(index) ?: return null

            val playUrl = RemoteHelper.getInstance().buildVideoUrl(videoData.Id)
            val historyEntity = PlayHistoryUtils.getPlayHistory(playUrl, MediaType.REMOTE_STORAGE)
            val position = RemoteMediaSourceHelper.getHistoryPosition(historyEntity)
            val (episodeId, danmuPath) = RemoteMediaSourceHelper.getVideoDanmu(
                historyEntity,
                videoData
            )
            val subtitlePath = RemoteMediaSourceHelper.getVideoSubtitle(historyEntity, videoData)
            return RemoteMediaSource(
                index,
                videoSources,
                playUrl,
                position,
                danmuPath,
                episodeId,
                subtitlePath
            )
        }
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

    override suspend fun indexSource(index: Int): GroupSource? {
        return videoSources.getOrNull(index)?.let {
            build(index, videoSources)
        }
    }

    override fun getVideoUrl(): String {
        return playUrl
    }

    override fun getVideoTitle(): String {
        return videoSources[index].getEpisodeName()
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)?.getEpisodeName() ?: ""
    }

    override fun getMediaType(): MediaType {
        return MediaType.REMOTE_STORAGE
    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }
}