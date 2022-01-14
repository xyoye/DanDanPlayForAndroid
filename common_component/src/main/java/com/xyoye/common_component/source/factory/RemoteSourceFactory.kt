package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.RemoteMediaSource
import com.xyoye.common_component.utils.*
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2022/1/12
 */
object RemoteSourceFactory {

    suspend fun create(builder: VideoSourceFactory.Builder): RemoteMediaSource? {
        val videoSources = builder.videoSources.filterIsInstance<RemoteVideoData>()
        val videoData = videoSources.getOrNull(builder.index) ?: return null

        val playUrl = RemoteHelper.getInstance().buildVideoUrl(videoData.Id)
        val historyEntity = PlayHistoryUtils.getPlayHistory(playUrl, MediaType.REMOTE_STORAGE)

        val position = getHistoryPosition(historyEntity)
        val (episodeId, danmuPath) = getVideoDanmu(historyEntity, videoData)
        val subtitlePath = getVideoSubtitle(historyEntity, videoData)
        return RemoteMediaSource(
            builder.index,
            videoSources,
            playUrl,
            position,
            danmuPath,
            episodeId,
            subtitlePath
        )
    }

    private fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    private suspend fun getVideoDanmu(
        history: PlayHistoryEntity?,
        videoData: RemoteVideoData
    ): Pair<Int, String?> {
        //从播放记录读取弹幕
        if (history?.danmuPath != null) {
            return Pair(history.episodeId, history.danmuPath)
        }

        //自动匹配同文件夹内同名弹幕
        if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
            try {
                val danmuResponseBody = Retrofit.remoteService.downloadDanmu(videoData.Hash)
                val videoName = videoData.getEpisodeName()
                val danmuFileName = getFileNameNoExtension(videoName) + ".xml"
                val danmuPath = DanmuUtils.saveDanmu(
                    danmuFileName,
                    danmuResponseBody.byteStream()
                )
                return Pair(0, danmuPath)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return Pair(0, null)
    }

    private suspend fun getVideoSubtitle(
        history: PlayHistoryEntity?,
        videoData: RemoteVideoData
    ): String? {
        //从播放记录读取弹幕
        if (history?.subtitlePath != null) {
            return history.subtitlePath
        }

        //自动匹配同文件夹内同名字幕
        if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()) {
            try {
                val subtitleData = Retrofit.remoteService.searchSubtitle(videoData.Id)
                if (subtitleData.subtitles.isNotEmpty()) {
                    val subtitleName = subtitleData.subtitles[0].fileName
                    val subtitleResponseBody =
                        Retrofit.remoteService.downloadSubtitle(videoData.Id, subtitleName)
                    return SubtitleUtils.saveSubtitle(
                        subtitleName,
                        subtitleResponseBody.byteStream()
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        return null
    }
}