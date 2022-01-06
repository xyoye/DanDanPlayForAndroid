package com.xyoye.common_component.source.media

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.source.helper.SourceHelper
import com.xyoye.common_component.source.helper.WebDavMediaSourceHelper
import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.GroupSource
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.enums.MediaType
import com.xyoye.sardine.DavResource

/**
 * Created by xyoye on 2021/11/16.
 */

class WebDavMediaSource private constructor(
    private val rootPath: String,
    private val authHeader: Map<String, String>,
    private val index: Int,
    private val videoSources: List<DavResource>,
    private val extSources: List<DavResource>,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?
) : GroupVideoSource(index, videoSources), ExtraSource {

    companion object {

        suspend fun build(
            rootPath: String,
            authHeader: Map<String, String>,
            index: Int,
            videoSources: List<DavResource>,
            extSources: List<DavResource>
        ): WebDavMediaSource? {
            val davResource = videoSources.getOrNull(index) ?: return null

            val videoUrl = rootPath + davResource.href.toASCIIString()
            val history = PlayHistoryUtils.getPlayHistory(videoUrl, MediaType.WEBDAV_SERVER)
            val position = WebDavMediaSourceHelper.getHistoryPosition(history)
            val (episodeId, danmuPath) = WebDavMediaSourceHelper.getVideoDanmu(
                davResource,
                extSources,
                rootPath,
                authHeader,
                history
            )
            val subtitlePath = WebDavMediaSourceHelper.getVideoSubtitle(
                davResource,
                extSources,
                rootPath,
                authHeader,
                history
            )

            return WebDavMediaSource(
                rootPath,
                authHeader,
                index,
                videoSources,
                extSources,
                position,
                danmuPath,
                episodeId,
                subtitlePath
            )
        }
    }

    override fun getVideoUrl(): String {
        return rootPath + videoSources[index].href.toASCIIString()
    }

    override fun getVideoTitle(): String {
        return getFileName(videoSources[index].name).formatFileName()
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun indexTitle(index: Int): String {
        return videoSources.getOrNull(index)?.name?.let {
            getFileName(it).formatFileName()
        } ?: ""
    }

    override fun getMediaType(): MediaType {
        return MediaType.WEBDAV_SERVER
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

    override fun getHttpHeader(): Map<String, String> {
        return authHeader
    }

    override fun getUniqueKey(): String {
        val url = getVideoUrl()
        return "webdav:/${SourceHelper.getHttpUniqueKey(url)}"
    }

    override suspend fun indexSource(index: Int): GroupSource? {
        if (index in videoSources.indices)
            return build(rootPath, authHeader, index, videoSources, extSources)
        return null
    }
}