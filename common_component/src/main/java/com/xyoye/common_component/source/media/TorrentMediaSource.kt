package com.xyoye.common_component.source.media

import com.xunlei.downloadlib.parameter.TorrentFileInfo
import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.GroupSource
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/14.
 */

class TorrentMediaSource private constructor(
    private val index: Int,
    private val videoSources: List<TorrentFileInfo>,
    private val playUrl: String,
    private val torrentPath: String,
    private var currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?
) : GroupVideoSource(index, videoSources), ExtraSource {

    companion object {
        suspend fun build(
            index: Int,
            torrentPath: String,
        ): TorrentMediaSource? {
            val (playUrl, torrentFileInfoList) = ThunderManager.getInstance().torrent2PlayUrl(
                torrentPath,
                PathHelper.getPlayCacheDirectory(),
                index
            )
            if (playUrl.isNullOrEmpty())
                return null

            val history = PlayHistoryUtils.getPlayHistory(playUrl, MediaType.MAGNET_LINK)
            return TorrentMediaSource(
                index,
                torrentFileInfoList,
                playUrl,
                torrentPath,
                history?.videoPosition ?: 0,
                history?.danmuPath,
                history?.episodeId ?: 0,
                history?.subtitlePath
            )
        }
    }

    override fun getVideoUrl(): String {
        return playUrl
    }

    override fun getVideoTitle(): String {
        return videoSources[index].mFileName
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun indexTitle(index: Int): String {
        return videoSources[index].mFileName
    }

    override fun getMediaType(): MediaType {
        return MediaType.MAGNET_LINK
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

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }

    override suspend fun indexSource(index: Int): GroupSource? {
        return build(index, torrentPath)
    }

    fun getPlayTaskId(): Long {
        return ThunderManager.getInstance().getTaskId(torrentPath)
    }

    fun getTorrentPath(): String {
        return torrentPath
    }

    fun getTorrentIndex(): Int {
        return index
    }
}