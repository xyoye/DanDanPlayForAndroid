package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.SmbMediaSource
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.server.SMBPlayServer
import com.xyoye.common_component.utils.smb.SMBFile
import com.xyoye.common_component.utils.smb.v2.SMBJManager
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2022/1/12
 */
object SmbSourceFactory {

    suspend fun create(builder: VideoSourceFactory.Builder): SmbMediaSource? {
        val videoSources = builder.videoSources.filterIsInstance<SMBFile>()
        val extSources = builder.extraSources.filterIsInstance<SMBFile>()

        val smbFile = videoSources.getOrNull(builder.index) ?: return null
        val proxyUrl = createProxyUrl(builder.rootPath, smbFile)
        val history = PlayHistoryUtils.getPlayHistory(proxyUrl, MediaType.SMB_SERVER)
        val position = getHistoryPosition(history)
        val (episodeId, danmuPath) = getVideoDanmu(history, builder.rootPath, smbFile, extSources)
        val subtitlePath = getVideoSubtitle(history, builder.rootPath, smbFile, extSources)

        return SmbMediaSource(
            builder.index,
            videoSources,
            extSources,
            builder.rootPath,
            proxyUrl,
            position,
            danmuPath,
            episodeId,
            subtitlePath
        )
    }

    fun generateUniqueKey(rootPath: String, smbFile: SMBFile): String {
        return (rootPath + "_" + smbFile.name).toMd5String()
    }

    private fun createProxyUrl(rootPath: String, smbFile: SMBFile): String {
        val filePath = "$rootPath\\${smbFile.name}"
        return SMBPlayServer.getInstance().getInputStreamUrl(filePath, smbFile.size) {
            SMBJManager.getInstance().getInputStream(it)
        }
    }

    private fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    private fun getVideoDanmu(
        history: PlayHistoryEntity?,
        rootPath: String,
        smbFile: SMBFile,
        extSources: List<SMBFile>
    ): Pair<Int, String?> {
        //从播放记录读取弹幕
        if (history?.danmuPath != null) {
            return Pair(history.episodeId, history.danmuPath)
        }

        //匹配同文件夹内同名弹幕
        if (DanmuConfig.isAutoLoadSameNameDanmu()) {
            //目标文件名
            val targetFileName = getFileNameNoExtension(smbFile.name) + ".xml"
            val targetFile = extSources.find { it.name == targetFileName }

            if (targetFile != null) {
                val danmuInputStream = SMBJManager.getInstance().getInputStream(
                    "$rootPath\\${targetFile.name}"
                )
                val danmuPath = DanmuUtils.saveDanmu(targetFile.name, danmuInputStream)
                return Pair(0, danmuPath)
            }
        }

        return Pair(0, null)
    }

    private fun getVideoSubtitle(
        history: PlayHistoryEntity?,
        rootPath: String,
        smbFile: SMBFile,
        extSources: List<SMBFile>
    ): String? {
        //从播放记录读取弹幕
        if (history?.subtitlePath != null) {
            return history.subtitlePath
        }

        //匹配同文件夹内同名字幕
        if (SubtitleConfig.isAutoLoadSameNameSubtitle()) {
            val videoFileName = getFileNameNoExtension(smbFile.name) + "."
            val targetFile = extSources.find {
                SubtitleUtils.isSameNameSubtitle(it.name, videoFileName)
            }
            if (targetFile != null) {
                val targetFilePath = "$rootPath\\${targetFile.name}"
                val subtitleInputStream = SMBJManager.getInstance().getInputStream(targetFilePath)
                return SubtitleUtils.saveSubtitle(targetFile.name, subtitleInputStream)
            }
        }

        return null
    }
}