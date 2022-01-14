package com.xyoye.common_component.source.factory

import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.TorrentMediaSource
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2022/1/11
 */
object TorrentSourceFactory {

    suspend fun create(builder: VideoSourceFactory.Builder): TorrentMediaSource? {
        val (playUrl, torrentFileInfoList) = ThunderManager.getInstance().torrent2PlayUrl(
            builder.rootPath,
            PathHelper.getPlayCacheDirectory(),
            builder.index
        )
        if (playUrl.isNullOrEmpty())
            return null

        val history = PlayHistoryUtils.getPlayHistory(playUrl, MediaType.MAGNET_LINK)
        return TorrentMediaSource(
            builder.index,
            torrentFileInfoList,
            playUrl,
            builder.rootPath,
            history?.videoPosition ?: 0,
            history?.danmuPath,
            history?.episodeId ?: 0,
            history?.subtitlePath
        )
    }

    fun generateUniqueKey(torrentPath: String, index: Int): String {
        val hash = getFileNameNoExtension(torrentPath)
        return "${hash}_${index}".toMd5String()
    }
}