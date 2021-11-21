package com.xyoye.common_component.source.helper

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2021/11/21.
 */

object RemoteMediaSourceHelper {

    fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    suspend fun getVideoDanmu(
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

    suspend fun getVideoSubtitle(
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