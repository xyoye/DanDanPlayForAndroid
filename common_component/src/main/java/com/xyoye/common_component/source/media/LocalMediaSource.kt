package com.xyoye.common_component.source.media

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.MediaSource
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/14.
 */

class LocalMediaSource private constructor(
    private val index: Int,
    private val playGroup: List<VideoEntity>,
    private val currentPosition: Long,
    private val danmuPath: String?,
    private val episodeId: Int,
    private val subtitlePath: String?
) : MediaSource(index, playGroup) {

    companion object {

        suspend fun build(index: Int, playGroup: List<VideoEntity>?): LocalMediaSource? {
            val video = playGroup?.getOrNull(index) ?: return null

            val position = getHistoryPosition(video)
            val (episodeId, danmuPath) = if (video.danmuPath != null) {
                Pair(video.danmuId, video.danmuPath)
            } else {
                getVideoDanmu(video)
            }
            val subtitlePath = video.subtitlePath ?: getVideoSubtitle(video)
            return LocalMediaSource(
                index,
                playGroup,
                position,
                danmuPath,
                episodeId,
                subtitlePath
            )
        }

        private suspend fun getHistoryPosition(video: VideoEntity): Long {
            return DatabaseManager.instance
                .getPlayHistoryDao()
                .getPlayHistoryPosition(video.filePath, MediaType.LOCAL_STORAGE)
                ?: 0L
        }

        private suspend fun getVideoDanmu(video: VideoEntity): Pair<Int, String?> {
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

        private suspend fun getVideoSubtitle(video: VideoEntity): String? {
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

    override fun getVideoUrl(): String {
        return playGroup[index].filePath
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
        playGroup[index].danmuPath = path
    }

    override fun getEpisodeId(): Int {
        return episodeId
    }

    override fun setEpisodeId(id: Int) {
        playGroup[index].danmuId = id
    }

    override fun getSubtitlePath(): String? {
        return subtitlePath
    }

    override fun setSubtitlePath(path: String) {
        playGroup[index].subtitlePath = path
    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }

    override fun getMediaType(): MediaType {
        return MediaType.LOCAL_STORAGE
    }

    override fun indexTitle(index: Int): String {
        return playGroup.getOrNull(index)?.filePath?.run {
            getFileName(this)
        } ?: ""
    }

    override suspend fun indexSource(index: Int): MediaSource? {
        if (index in playGroup.indices)
            return build(index, playGroup)
        return null
    }
}