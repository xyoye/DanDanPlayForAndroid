package com.xyoye.common_component.source.media

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.source.helper.FTPMediaSourceHelper
import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.GroupSource
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.server.FTPPlayServer
import com.xyoye.data_component.enums.MediaType
import org.apache.commons.net.ftp.FTPFile

/**
 * Created by xyoye on 2021/11/21.
 */

class FTPMediaSource private constructor(
    private val index: Int,
    private val videoSources: List<FTPFile>,
    private val extSources: List<FTPFile>,
    private val rootPath: String,
    private val proxyUrl: String,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?
) : GroupVideoSource(index, videoSources), ExtraSource {

    companion object {

        suspend fun build(
            index: Int,
            videoSources: List<FTPFile>,
            extSources: List<FTPFile>,
            rootPath: String,
        ): FTPMediaSource? {
            val ftpFile = videoSources.getOrNull(index) ?: return null

            val proxyUrl = FTPPlayServer.getInstance().getInputStreamUrl(ftpFile.name)
            val history = PlayHistoryUtils.getPlayHistory(proxyUrl, MediaType.FTP_SERVER)
            val position = FTPMediaSourceHelper.getHistoryPosition(history)
            val (episodeId, danmuPath) = FTPMediaSourceHelper
                .getVideoDanmu(history, rootPath, ftpFile, extSources)
            val subtitlePath = FTPMediaSourceHelper
                .getVideoSubtitle(history, rootPath, ftpFile, extSources)

            if (FTPMediaSourceHelper.fillPlaySource(rootPath, ftpFile).not()) {
                return null
            }

            return FTPMediaSource(
                index,
                videoSources,
                extSources,
                rootPath,
                proxyUrl,
                position,
                danmuPath,
                episodeId,
                subtitlePath
            )
        }
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

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)?.name?.formatFileName() ?: ""
    }

    override suspend fun indexSource(index: Int): GroupSource? {
        if (index in videoSources.indices)
            return build(index, videoSources, extSources, rootPath)
        return null
    }

    override fun getVideoUrl(): String {
        return proxyUrl
    }

    override fun getVideoTitle(): String {
        return getFileName(videoSources[index].name).formatFileName()
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun getMediaType(): MediaType {
        return MediaType.FTP_SERVER
    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }

}