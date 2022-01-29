package com.xyoye.common_component.source.factory

import android.text.TextUtils
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.WebDavMediaSource
import com.xyoye.common_component.utils.*
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.sardine.DavResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Created by xyoye on 2022/1/11
 */
object WebDavSourceFactory {

    suspend fun create(builder: VideoSourceFactory.Builder): WebDavMediaSource? {
        val videoSources = builder.videoSources.filterIsInstance<DavResource>()
        val extSources = builder.extraSources.filterIsInstance<DavResource>()

        val davResource = videoSources.getOrNull(builder.index)
            ?: return null

        val uniqueKey = generateUniqueKey(builder.rootPath, videoSources[builder.index])
        val history = PlayHistoryUtils.getPlayHistory(uniqueKey, MediaType.WEBDAV_SERVER)

        val position = getHistoryPosition(history)
        val (episodeId, danmuPath) = getVideoDanmu(
            davResource,
            extSources,
            builder.rootPath,
            builder.httpHeaders,
            history
        )
        val subtitlePath = getVideoSubtitle(
            davResource,
            extSources,
            builder.rootPath,
            builder.httpHeaders,
            history
        )

        return WebDavMediaSource(
            builder.rootPath,
            builder.httpHeaders,
            builder.index,
            videoSources,
            extSources,
            position,
            danmuPath,
            episodeId,
            subtitlePath,
            uniqueKey
        )
    }

    fun generateUniqueKey(rootPath: String, davResource: DavResource): String {
        return (rootPath + "_" + davResource.href.toASCIIString()).toMd5String()
    }

    private fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    private suspend fun getVideoDanmu(
        davResource: DavResource,
        extSources: List<DavResource>,
        rootPath: String,
        header: Map<String, String>,
        history: PlayHistoryEntity?
    ): Pair<Int, String?> {
        //从播放记录读取弹幕
        if (TextUtils.isEmpty(history?.danmuPath).not()) {
            return Pair(history!!.episodeId, history.danmuPath)
        }

        //匹配同文件夹内同名弹幕
        if (DanmuConfig.isAutoLoadSameNameDanmu()) {
            val danmuPath = findAndDownloadDanmu(
                davResource,
                extSources,
                rootPath,
                header
            )
            if (danmuPath != null) {
                return Pair(0, danmuPath)
            }
        }
        return Pair(0, null)
    }

    private suspend fun getVideoSubtitle(
        davResource: DavResource,
        extSources: List<DavResource>,
        rootPath: String,
        header: Map<String, String>,
        history: PlayHistoryEntity?
    ): String? {
        //从播放记录读取字幕
        if (TextUtils.isEmpty(history?.subtitlePath).not()) {
            return history!!.subtitlePath
        }

        //匹配同文件夹内同名字幕
        if (SubtitleConfig.isAutoLoadSameNameSubtitle()) {
            val subtitlePath = findAndDownloadSubtitle(davResource, extSources, rootPath, header)
            if (subtitlePath != null) {
                return subtitlePath
            }
        }

        return null
    }

    private suspend fun findAndDownloadDanmu(
        davResource: DavResource,
        extSources: List<DavResource>,
        rootPath: String,
        header: Map<String, String>
    ): String? {
        return withContext(Dispatchers.IO) {
            //目标文件名
            val targetFileName = getFileNameNoExtension(davResource.name) + ".xml"
            //遍历当前目录
            val danmuDavSource =
                extSources.find { it.name == targetFileName } ?: return@withContext null

            //下载文件
            val url = rootPath + danmuDavSource.href.toASCIIString()
            try {
                val responseBody = Retrofit.extService.downloadResource(url, header)
                return@withContext DanmuUtils.saveDanmu(
                    danmuDavSource.name,
                    responseBody.byteStream()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext null
        }
    }

    private suspend fun findAndDownloadSubtitle(
        davResource: DavResource,
        extSources: List<DavResource>,
        rootPath: String,
        header: Map<String, String>
    ): String? {
        return withContext(Dispatchers.IO) {
            //视频文件名
            val videoFileName = getFileNameNoExtension(davResource.name) + "."
            //遍历当前目录
            val subtitleDavSource =
                extSources.find {
                    SubtitleUtils.isSameNameSubtitle(it.name, videoFileName)
                } ?: return@withContext null
            //下载文件
            val url = rootPath + subtitleDavSource.href.toASCIIString()
            try {
                val responseBody = Retrofit.extService.downloadResource(url, header)
                return@withContext SubtitleUtils.saveSubtitle(
                    subtitleDavSource.name,
                    responseBody.byteStream()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null
        }
    }
}