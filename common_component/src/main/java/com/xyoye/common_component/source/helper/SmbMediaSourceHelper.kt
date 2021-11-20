package com.xyoye.common_component.source.helper

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.FileHashUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.server.SMBPlayServer
import com.xyoye.common_component.utils.smb.SMBFile
import com.xyoye.common_component.utils.smb.v2.SMBJManager
import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2021/11/20.
 */

object SmbMediaSourceHelper {

    fun createProxyUrl(rootPath: String, smbFile: SMBFile): String {
        val filePath = "$rootPath\\${smbFile.name}"
        return SMBPlayServer.getInstance().getInputStreamUrl(filePath, smbFile.size) {
            SMBJManager.getInstance().getInputStream(it)
        }
    }

    fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    suspend fun getVideoDanmu(
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
        if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
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

        //匹配视频网络弹幕
        if (DanmuConfig.isAutoMatchDanmuNetworkStorage()) {
            val filePath = "$rootPath\\${smbFile.name}"
            val stream = SMBJManager.getInstance().getInputStream(filePath)
            val fileHash = FileHashUtils.getHash(stream)
            if (!fileHash.isNullOrEmpty()) {
                DanmuUtils.matchDanmuSilence(filePath, fileHash)?.let {
                    return Pair(it.second, it.first)
                }
            }
        }

        return Pair(0, null)
    }

    fun getVideoSubtitle(
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
        if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()) {
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