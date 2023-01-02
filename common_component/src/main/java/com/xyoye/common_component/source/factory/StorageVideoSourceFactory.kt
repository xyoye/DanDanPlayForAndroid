package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.extraSources
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileNameNoExtension

/**
 * Created by xyoye on 2023/1/2.
 */

object StorageVideoSourceFactory {

    suspend fun create(file: StorageFile, storage: Storage): StorageVideoSource? {
        val playUrl = storage.createPlayUrl(file) ?: return null
        val danmuInfo = getDanmuInfo(file, storage)
        val subtitlePath = getSubtitlePath(file, storage)
        return StorageVideoSource(
            playUrl,
            file,
            storage,
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
        if (DanmuConfig.isAutoLoadSameNameDanmu().not()) {
            return danmuNotFound
        }

        //是否存在同名弹幕文件
        val danmuFileName = getFileNameNoExtension(file.fileName()) + ".xml"
        val danmuFile = storage.extraSources.find {
            it.fileName() == danmuFileName
        } ?: return danmuNotFound


        //是否成功保存弹幕文件
        val danmuPath = storage.openFile(danmuFile)?.run {
            DanmuUtils.saveDanmu(danmuFileName, this)
        } ?: return danmuNotFound

        return Pair(0, danmuPath)
    }

    private suspend fun getSubtitlePath(file: StorageFile, storage: Storage): String? {
        val subtitleNotFound = null

        //从播放记录读取弹幕
        if (file.playHistory?.subtitlePath?.isNotEmpty() == true) {
            return file.playHistory?.subtitlePath
        }

        //是否匹配同文件夹内同名字幕
        if (SubtitleConfig.isAutoLoadSameNameSubtitle().not()) {
            return subtitleNotFound
        }

        //是否存在同名字幕文件
        val videoFileName = getFileNameNoExtension(file.fileName()) + "."
        val subtitleFile = storage.extraSources.find {
            SubtitleUtils.isSameNameSubtitle(it.fileName(), videoFileName)
        } ?: return subtitleNotFound


        //是否成功保存字幕文件
        val subtitlePath = storage.openFile(subtitleFile)?.run {
            SubtitleUtils.saveSubtitle(subtitleFile.fileName(), this)
        } ?: return subtitleNotFound

        return subtitlePath
    }
}