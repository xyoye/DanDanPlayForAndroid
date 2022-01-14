package com.xyoye.common_component.source.factory

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.FTPMediaSource
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.ftp.FTPManager
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.server.FTPPlayServer
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.InputStream


/**
 * Created by xyoye on 2022/1/11
 */
object FTPSourceFactory {

    suspend fun create(builder: VideoSourceFactory.Builder): FTPMediaSource? {
        val videoSources = builder.videoSources.filterIsInstance<FTPFile>()
        val extSources = builder.extraSources.filterIsInstance<FTPFile>()

        val ftpFile = videoSources.getOrNull(builder.index) ?: return null

        val proxyUrl = FTPPlayServer.getInstance().getInputStreamUrl(ftpFile.name)
        val history = PlayHistoryUtils.getPlayHistory(proxyUrl, MediaType.FTP_SERVER)
        val position = getHistoryPosition(history)
        val (episodeId, danmuPath) = getVideoDanmu(history, builder.rootPath, ftpFile, extSources)
        val subtitlePath = getVideoSubtitle(history, builder.rootPath, ftpFile, extSources)

        if (fillPlaySource(builder.rootPath, ftpFile).not()) {
            return null
        }

        return FTPMediaSource(
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

    fun generateUniqueKey(rootPath: String, ftpFile: FTPFile): String {
        return (rootPath + "_" + ftpFile.name).toMd5String()
    }

    private fun fillPlaySource(rootPath: String, ftpFile: FTPFile): Boolean {
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

    private fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    private fun getVideoDanmu(
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
        if (DanmuConfig.isAutoLoadSameNameDanmu()) {
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

        return Pair(0, null)
    }

    private fun getVideoSubtitle(
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
        if (SubtitleConfig.isAutoLoadSameNameSubtitle()) {
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