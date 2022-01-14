package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.LocalMediaSource
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2022/1/12
 */
object LocalSourceFactory {

    suspend fun create(builder: VideoSourceFactory.Builder): LocalMediaSource? {
        val videoSources = builder.videoSources.filterIsInstance<VideoEntity>()
        val video = videoSources.getOrNull(builder.index) ?: return null

        val (episodeId, danmuPath) = getVideoDanmu(video)
        val subtitlePath = getVideoSubtitle(video)
        val position = getHistoryPosition(video)
        return LocalMediaSource(
            builder.index,
            videoSources,
            position,
            danmuPath,
            episodeId,
            subtitlePath
        )
    }

    fun generateUniqueKey(entity: VideoEntity): String {
        return entity.filePath.toMd5String()
    }

    private suspend fun getHistoryPosition(video: VideoEntity): Long {
        return DatabaseManager.instance
            .getPlayHistoryDao()
            .getPlayHistoryPosition(video.filePath, MediaType.LOCAL_STORAGE)
            ?: 0L
    }

    private fun getVideoDanmu(video: VideoEntity): Pair<Int, String?> {
        //当前视频已绑定弹幕
        if (video.danmuPath != null) {
            return Pair(video.danmuId, video.danmuPath)
        }
        //从本地找同名弹幕
        if (DanmuConfig.isAutoLoadSameNameDanmu()) {
            DanmuUtils.findLocalDanmuByVideo(video.filePath)?.let {
                return Pair(0, it)
            }
        }
        return Pair(0, null)
    }

    private suspend fun getVideoSubtitle(video: VideoEntity): String? {
        //当前视频已绑定字幕
        if (video.subtitlePath != null) {
            return video.subtitlePath
        }
        //自动加载本地同名字幕
        if (SubtitleConfig.isAutoLoadSameNameSubtitle()) {
            SubtitleUtils.findLocalSubtitleByVideo(video.filePath)?.let {
                return it
            }
        }
        if (SubtitleConfig.isAutoMatchSubtitle()) {
            SubtitleUtils.matchSubtitleSilence(video.filePath)?.let {
                return it
            }
        }
        return null
    }
}