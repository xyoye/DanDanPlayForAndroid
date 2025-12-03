package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.StorageSortOption
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.data_component.bean.LocalDanmuBean

/**
 * Created by xyoye on 2023/1/2.
 */

object StorageVideoSourceFactory {

    suspend fun create(file: StorageFile): StorageVideoSource? {
        val storage = file.storage
        val videoSources = getVideoSources(storage)
        val playUrl = storage.createPlayUrl(file) ?: return null
        val danmu = findLocalDanmu(file, storage)
        val subtitlePath = getSubtitlePath(file, storage)
        val audioPath = file.playHistory?.audioPath
        return StorageVideoSource(
            playUrl,
            file,
            videoSources,
            danmu,
            subtitlePath,
            audioPath
        )
    }

    private suspend fun findLocalDanmu(file: StorageFile, storage: Storage): LocalDanmuBean? {
        //从播放记录读取弹幕
        val history = file.playHistory
        if (history?.danmuPath?.isNotEmpty() == true) {
            return LocalDanmuBean(history.danmuPath!!, history.episodeId)
        }

        //是否匹配同文件夹内同名弹幕
        if (DanmuConfig.getAutoLoadSameNameDanmu()) {
            return storage.cacheDanmu(file)
        }

        return null
    }

    private suspend fun getSubtitlePath(file: StorageFile, storage: Storage): String? {
        val subtitleNotFound = null

        //从播放记录读取弹幕
        if (file.playHistory?.subtitlePath?.isNotEmpty() == true) {
            return file.playHistory?.subtitlePath
        }

        //是否匹配同文件夹内同名字幕
        if (SubtitleConfig.getAutoLoadSameNameSubtitle()) {
            return storage.cacheSubtitle(file)
                ?: subtitleNotFound
        }

        return subtitleNotFound
    }

    private fun getVideoSources(storage: Storage): List<StorageFile> {
        return storage.directoryFiles
            .filter { it.isVideoFile() }
            .filter { AppConfig.getShowHiddenFile() || !it.fileName().startsWith(".") }
            .sortedWith(StorageSortOption.comparator())
    }
}