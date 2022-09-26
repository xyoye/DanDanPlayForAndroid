package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.ScreencastConfig
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.ScreencastMediaSource
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.data.screeencast.ScreencastVideoData
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import fi.iki.elonen.NanoHTTPD

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/16
 *     desc  :
 * </pre>
 */

object ScreencastSourceFactory {

    suspend fun create(builder: VideoSourceFactory.Builder): ScreencastMediaSource? {
        val videoSources = builder.videoSources
            .filterIsInstance<ScreencastData>()
            .toMutableList()
        val screencastData = videoSources.firstOrNull()
            ?: return null
        val video = screencastData.videos.getOrNull(builder.index)
            ?: return null

        val uniqueKey = generateUniqueKey(screencastData, video)
        val history = PlayHistoryUtils.getPlayHistory(uniqueKey, MediaType.SCREEN_CAST)

        val subtitlePath = getVideoSubtitle(screencastData, video, history)
        val (episodeId, danmuPath) = getVideoDanmu(screencastData, video, history)

        return ScreencastMediaSource(
            builder.index,
            screencastData,
            history?.videoPosition ?: 0,
            danmuPath,
            episodeId,
            subtitlePath,
            uniqueKey
        )
    }

    fun generateUniqueKey(screencastData: ScreencastData, videoData: ScreencastVideoData): String {
        return screencastData.getVideoUrl(videoData.videoIndex).toMd5String()
    }

    /**
     * 获取视频关联的弹幕
     */
    private suspend fun getVideoDanmu(
        screencastData: ScreencastData,
        video: ScreencastVideoData,
        history: PlayHistoryEntity?
    ): Pair<Int, String?> {
        //优先读取本地历史弹幕
        if (ScreencastConfig.isUseHistoryExtraSource()) {
            var danmuData = getDanmuFromLocal(history)
            if (danmuData.second.isNullOrEmpty()) {
                danmuData = getDanmuFromRemote(screencastData, video)
            }
            return danmuData
        }

        //优先读取远端投屏弹幕
        var danmuData = getDanmuFromRemote(screencastData, video)
        if (danmuData.second.isNullOrEmpty()) {
            danmuData = getDanmuFromLocal(history)
        }
        return danmuData
    }

    /**
     * 获取视频关联的弹幕，从投屏远端
     */
    private suspend fun getDanmuFromRemote(
        screencastData: ScreencastData,
        video: ScreencastVideoData
    ): Pair<Int, String?> {
        val defaultResult = Pair(0, null)

        val danmuUrl = screencastData.getDanmuUrl(video.videoIndex)
        try {
            val response = Retrofit.extService.downloadResourceWithHeader(danmuUrl)
            if (response.code() != NanoHTTPD.Response.Status.OK.requestStatus) return defaultResult
            val responseBody = response.body() ?: return defaultResult

            val danmuPath = DanmuUtils.saveDanmu(
                fileName = "${getFileNameNoExtension(video.videoTitle)}.xml",
                inputStream = responseBody.byteStream()
            )
            val episodeId = response.headers()["episodeId"]?.toIntOrNull() ?: 0
            return Pair(episodeId, danmuPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defaultResult
    }

    /**
     * 获取视频关联的弹幕，从本地历史
     */
    private fun getDanmuFromLocal(history: PlayHistoryEntity?): Pair<Int, String?> {
        val defaultResult = Pair(0, null)

        val danmuPath = history?.danmuPath
        if (danmuPath.isNullOrEmpty()) {
            return defaultResult
        }

        return Pair(history.episodeId, danmuPath)
    }

    /**
     * 获取视频关联的字幕
     */
    private suspend fun getVideoSubtitle(
        screencastData: ScreencastData,
        video: ScreencastVideoData,
        history: PlayHistoryEntity?
    ): String? {
        //优先读取本地历史字幕
        if (ScreencastConfig.isUseHistoryExtraSource()) {
            var subtitle = getSubtitleFromLocal(history)
            if (subtitle.isNullOrEmpty()) {
                subtitle = getSubtitleFromRemote(screencastData, video)
            }
            return subtitle
        }

        //优先读取远端投屏字幕
        var subtitle = getSubtitleFromRemote(screencastData, video)
        if (subtitle.isNullOrEmpty()) {
            subtitle = getSubtitleFromLocal(history)
        }
        return subtitle
    }

    /**
     * 获取视频关联的字幕，从投屏远端
     */
    private suspend fun getSubtitleFromRemote(
        screencastData: ScreencastData,
        video: ScreencastVideoData
    ): String? {
        val subtitleUrl = screencastData.getSubtitleUrl(video.videoIndex)

        try {
            val response = Retrofit.extService.downloadResourceWithHeader(subtitleUrl)
            if (response.code() != NanoHTTPD.Response.Status.OK.requestStatus) return null
            val responseBody = response.body() ?: return null

            val subtitleSuffix = response.headers()["subtitleSuffix"] ?: "ass"
            val subtitleFileName = "${getFileNameNoExtension(video.videoTitle)}.${subtitleSuffix}"
            return SubtitleUtils.saveSubtitle(
                subtitleFileName,
                responseBody.byteStream()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 获取视频关联的字幕，从本地历史
     */
    private fun getSubtitleFromLocal(history: PlayHistoryEntity?): String? {
        val subtitlePath = history?.subtitlePath
        if (subtitlePath.isNullOrEmpty()) {
            return null
        }

        return subtitlePath
    }
}