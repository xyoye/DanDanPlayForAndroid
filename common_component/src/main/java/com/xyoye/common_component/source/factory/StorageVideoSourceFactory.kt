package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.file.StorageFile

/**
 * Created by xyoye on 2023/1/2.
 */

object StorageVideoSourceFactory {

    suspend fun create(file: StorageFile): StorageVideoSource? {
        val storage = file.storage
        val playUrl = storage.createPlayUrl(file) ?: return null
        val danmuInfo = getDanmuInfo(file, storage)
        val subtitlePath = getSubtitlePath(file, storage)
        return StorageVideoSource(
            playUrl,
            file,
            danmuInfo.first,
            danmuInfo.second,
            subtitlePath
        )
    }

    private suspend fun getDanmuInfo(file: StorageFile, storage: Storage): Pair<Int, String?> {
        val danmuNotFound = Pair(0, null)

        //从播放记录读取弹幕
        val history = file.playHistory
        if (history?.danmuPath?.isNotEmpty() == true) {
            return Pair(history.episodeId, history.danmuPath)
        }

        //是否匹配同文件夹内同名弹幕
        if (DanmuConfig.isAutoLoadSameNameDanmu()) {
            return storage.cacheDanmu(file)?.run { Pair(0, this) }
                ?: danmuNotFound
        }

        return danmuNotFound
    }

    private suspend fun getSubtitlePath(file: StorageFile, storage: Storage): String? {
        val subtitleNotFound = null

        //从播放记录读取弹幕
        if (file.playHistory?.subtitlePath?.isNotEmpty() == true) {
            return file.playHistory?.subtitlePath
        }

        //是否匹配同文件夹内同名字幕
        if (SubtitleConfig.isAutoLoadSameNameSubtitle()) {
            return storage.cacheSubtitle(file)
                ?: subtitleNotFound
        }

        return subtitleNotFound
    }
}