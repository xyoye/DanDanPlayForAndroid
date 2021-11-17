package com.xyoye.common_component.source.helper

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2021/11/16.
 */

object LocalMediaSourceHelper {
    suspend fun getHistoryPosition(video: VideoEntity): Long {
        return DatabaseManager.instance
            .getPlayHistoryDao()
            .getPlayHistoryPosition(video.filePath, MediaType.LOCAL_STORAGE)
            ?: 0L
    }

    suspend fun getVideoDanmu(video: VideoEntity): Pair<Int, String?> {
        //当前视频已绑定弹幕
        if (video.danmuPath != null) {
            return Pair(video.danmuId, video.danmuPath)
        }
        //从本地找同名弹幕
        if (DanmuConfig.isAutoLoadLocalDanmu()) {
            DanmuUtils.findLocalDanmuByVideo(video.filePath)?.let {
                return Pair(0, it)
            }
        }
        //自动加载网络弹幕
        if (DanmuConfig.isAutoLoadNetworkDanmu()) {
            val fileHash = IOUtils.getFileHash(video.filePath)
            if (!fileHash.isNullOrEmpty()) {
                DanmuUtils.matchDanmuSilence(video.filePath, fileHash)?.let {
                    return Pair(it.second, it.first)
                }
            }
        }
        return Pair(0, null)
    }

    suspend fun getVideoSubtitle(video: VideoEntity): String? {
        //当前视频已绑定字幕
        if (video.subtitlePath != null) {
            return video.subtitlePath
        }
        //自动加载本地同名字幕
        if (SubtitleConfig.isAutoLoadLocalSubtitle()) {
            SubtitleUtils.findLocalSubtitleByVideo(video.filePath)?.let {
                return it
            }
        }
        //自动加载网络字幕
        if (SubtitleConfig.isAutoLoadNetworkSubtitle()) {
            SubtitleUtils.matchSubtitleSilence(video.filePath)?.let {
                return it
            }
        }
        return null
    }
}