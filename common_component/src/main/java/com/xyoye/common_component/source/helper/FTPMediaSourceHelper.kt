package com.xyoye.common_component.source.helper

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.ftp.FTPManager
import com.xyoye.common_component.utils.server.FTPPlayServer
import com.xyoye.data_component.entity.PlayHistoryEntity
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.InputStream

/**
 * Created by xyoye on 2021/11/20.
 */

object FTPMediaSourceHelper {

    fun fillPlaySource(rootPath: String, ftpFile: FTPFile): Boolean {
        val inputStream: InputStream?
        try {
            inputStream = FTPManager.getInstance().getInputStream(rootPath, ftpFile.name)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        FTPPlayServer.getInstance().setPlaySource(ftpFile.name, ftpFile.size, inputStream)
        return true
    }

    fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    suspend fun getVideoDanmu(
        history: PlayHistoryEntity?,
        rootPath: String,
        ftpFile: FTPFile,
        extSources: List<FTPFile>
    ): Pair<Int, String?> {
        //从播放记录读取弹幕
        if (history?.danmuPath != null) {
            return Pair(history.episodeId, history.danmuPath)
        }

        //匹配同文件夹内同名弹幕
        if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
            val targetFileName = getFileNameNoExtension(ftpFile.name) + ".xml"
            val danmuFTPFile = extSources.find { it.name == targetFileName }

            if (danmuFTPFile != null) {
                val danmuFile = File(PathHelper.getDanmuDirectory(), targetFileName)
                val copySuccess =
                    FTPManager.getInstance().copyFtpFile(rootPath, danmuFTPFile.name, danmuFile)
                if (copySuccess) {
                    return Pair(0, danmuFile.absolutePath)
                }
            }
        }

        //匹配视频网络弹幕
        if (DanmuConfig.isAutoMatchDanmuNetworkStorage()) {
            val stream = FTPManager.getInstance().getInputStream(rootPath, ftpFile.name)
            val fileHash = FileHashUtils.getHash(stream)
            FTPManager.getInstance().disconnect()
            if (!fileHash.isNullOrEmpty()) {
                //根据hash匹配弹幕
                DanmuUtils.matchDanmuSilence(ftpFile.name, fileHash)?.let {
                    return Pair(it.second, it.first)
                }
            }
        }

        return Pair(0, null)
    }

    fun getVideoSubtitle(
        history: PlayHistoryEntity?,
        rootPath: String,
        ftpFile: FTPFile,
        extSources: List<FTPFile>
    ): String? {
        //从播放记录读取弹幕
        if (history?.subtitlePath != null) {
            return history.subtitlePath
        }

        //匹配同文件夹内同名字幕
        if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()) {
            val videoFileName = getFileNameNoExtension(ftpFile.name) + "."

            val danmuFTPFile = extSources.find {
                SubtitleUtils.isSameNameSubtitle(it.name, videoFileName)
            } ?: return null

            val subtitleFile = File(
                PathHelper.getSubtitleDirectory(), danmuFTPFile.name.formatFileName()
            )

            val copySuccess = FTPManager.getInstance()
                .copyFtpFile(rootPath, danmuFTPFile.name, subtitleFile)
            if (copySuccess) {
                return subtitleFile.absolutePath
            }
        }

        return null
    }
}