package com.xyoye.common_component.source.media

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.source.helper.SmbMediaSourceHelper
import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.GroupSource
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.smb.SMBFile
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2021/11/18
 */
class SmbMediaSource private constructor(
    private val index: Int,
    private val videoSources: List<SMBFile>,
    private val extSources: List<SMBFile>,
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
            videoSources: List<SMBFile>,
            extSources: List<SMBFile>,
            rootPath: String,
        ): SmbMediaSource? {
            val smbFile = videoSources.getOrNull(index) ?: return null
            val proxyUrl = SmbMediaSourceHelper.createProxyUrl(rootPath, smbFile)
            val history = PlayHistoryUtils.getPlayHistory(proxyUrl, MediaType.SMB_SERVER)
            val position = SmbMediaSourceHelper.getHistoryPosition(history)
            val (episodeId, danmuPath) = SmbMediaSourceHelper
                .getVideoDanmu(history, rootPath, smbFile, extSources)
            val subtitlePath = SmbMediaSourceHelper
                .getVideoSubtitle(history, rootPath, smbFile, extSources)

            return SmbMediaSource(
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

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)?.name?.formatFileName() ?: ""
    }

    override fun getMediaType(): MediaType {
        return MediaType.SMB_SERVER
    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }
}