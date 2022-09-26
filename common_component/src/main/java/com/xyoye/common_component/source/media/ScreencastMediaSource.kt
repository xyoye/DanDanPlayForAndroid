package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.enums.MediaType

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/16
 *     desc  :
 * </pre>
 */

class ScreencastMediaSource(
    private val index: Int,
    private val screencastData: ScreencastData,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?,
    private val uniqueKey: String
) : BaseVideoSource(index, screencastData.videos) {

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

    override fun indexTitle(index: Int): String {
        return screencastData.videos.getOrNull(index)?.videoTitle ?: ""
    }

    override suspend fun indexSource(index: Int): BaseVideoSource? {
        val source = VideoSourceFactory.Builder()
            .setVideoSources(listOf(screencastData))
            .setIndex(index)
            .create(getMediaType())
            ?: return null
        return source as ScreencastMediaSource
    }

    override fun getVideoUrl(): String {
        return screencastData.getVideoUrl(index)
    }

    override fun getVideoTitle(): String {
        return screencastData.videos.getOrNull(index)?.videoTitle ?: ""
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun getMediaType(): MediaType {
        return MediaType.SCREEN_CAST
    }

    override fun getUniqueKey(): String {
        return uniqueKey
    }

    override fun getHttpHeader(): Map<String, String>? {
        return screencastData.httpHeader
    }

    /**
     * 投屏的视频资源类型
     */
    fun getProvideMediaType(): MediaType {
        return MediaType.fromValue(screencastData.mediaType)
    }
}