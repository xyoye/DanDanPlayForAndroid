package com.xyoye.common_component.source.factory

import com.xyoye.common_component.extension.md5
import com.xyoye.common_component.extension.toFile
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
        //历史使用的弹幕
        val historyDanmu = Pair(history?.episodeId ?: 0, history?.danmuPath)
        //投屏远端弹幕地址
        val danmuUrl = screencastData.getDanmuUrl(video.videoIndex)
        try {
            val response = Retrofit.extService.downloadResourceWithHeader(danmuUrl)
            if (response.code() != NanoHTTPD.Response.Status.OK.requestStatus) return historyDanmu
            val responseBody = response.body() ?: return historyDanmu

            //投屏弹幕与历史弹幕相同，使用历史弹幕
            if (isSameMd5(response.headers()["danmuMd5"], historyDanmu.second)) {
                return historyDanmu
            }

            //下载并使用投屏弹幕
            val danmuPath = DanmuUtils.saveDanmu(
                fileName = "${getFileNameNoExtension(video.videoTitle)}.xml",
                inputStream = responseBody.byteStream()
            )
            val episodeId = response.headers()["episodeId"]?.toIntOrNull() ?: 0
            return Pair(episodeId, danmuPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //使用历史弹幕
        return historyDanmu
    }

    /**
     * 获取视频关联的字幕
     */
    private suspend fun getVideoSubtitle(
        screencastData: ScreencastData,
        video: ScreencastVideoData,
        history: PlayHistoryEntity?
    ): String? {
        //历史使用的字幕
        val historySubtitle = history?.subtitlePath
        //投屏远端字幕地址
        val remoteSubtitleUrl = screencastData.getSubtitleUrl(video.videoIndex)

        try {
            val response = Retrofit.extService.downloadResourceWithHeader(remoteSubtitleUrl)
            if (response.code() != NanoHTTPD.Response.Status.OK.requestStatus) return historySubtitle
            val responseBody = response.body() ?: return historySubtitle

            //投屏字幕与历史字幕相同，使用历史字幕
            if (isSameMd5(response.headers()["subtitleMd5"], historySubtitle)) {
                return historySubtitle
            }

            //下载并使用投屏字幕
            val subtitleSuffix = response.headers()["subtitleSuffix"] ?: "ass"
            val subtitleFileName = "${getFileNameNoExtension(video.videoTitle)}.${subtitleSuffix}"
            return SubtitleUtils.saveSubtitle(
                subtitleFileName,
                responseBody.byteStream()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //使用历史字幕
        return historySubtitle
    }

    /**
     * 判断文件MD5与目标MD5值是否相同
     */
    private fun isSameMd5(targetMd5: String?, sourceFilePath: String?): Boolean {
        if (targetMd5 == null || targetMd5.isEmpty()) {
            return false
        }
        val sourceMd5 = sourceFilePath?.toFile()?.md5()
        return targetMd5 == sourceMd5
    }
}