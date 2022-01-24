package com.xyoye.common_component.source.media

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.enums.MediaType
import com.xyoye.sardine.DavResource

/**
 * Created by xyoye on 2021/11/16.
 */

class WebDavMediaSource(
    private val rootPath: String,
    private val authHeader: Map<String, String>,
    private val index: Int,
    private val videoSources: List<DavResource>,
    private val extSources: List<DavResource>,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?,
    private val uniqueKey: String
) : BaseVideoSource(index, videoSources) {

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
        return uniqueKey
    }

    override suspend fun indexSource(index: Int): BaseVideoSource? {
        if (index in videoSources.indices) {
            val source = VideoSourceFactory.Builder()
                .setVideoSources(videoSources)
                .setExtraSource(extSources)
                .setRootPath(rootPath)
                .setHttpHeaders(authHeader)
                .setIndex(index)
                .create(getMediaType())
                ?: return null
            return source as WebDavMediaSource
        }
        return null
    }
}