package com.xyoye.common_component.source.helper

import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.FileHashUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.sardine.DavResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by xyoye on 2021/11/16.
 */

object WebDavMediaSourceHelper {

    fun getHistoryPosition(entity: PlayHistoryEntity?): Long {
        return entity?.videoPosition ?: 0L
    }

    suspend fun getVideoDanmu(
        davResource: DavResource,
        extSources: List<DavResource>,
        rootPath: String,
        header: Map<String, String>,
        history: PlayHistoryEntity?
    ): Pair<Int, String?> {
        //从播放记录读取弹幕
        if (history?.danmuPath != null) {
            return Pair(history.episodeId, history.danmuPath)
        }

        //匹配同文件夹内同名弹幕
        if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
            val danmuPath = findAndDownloadDanmu(davResource, extSources, rootPath, header)
            if (danmuPath != null) {
                return Pair(0, danmuPath)
            }
        }

        //匹配视频网络弹幕
        if (DanmuConfig.isAutoMatchDanmuNetworkStorage()) {
            val matchResult = matchAndDownloadDanmu(davResource, rootPath, header)
            if (matchResult != null) {
                return Pair(matchResult.second, matchResult.first)
            }
        }
        return Pair(0, null)
    }

    suspend fun getVideoSubtitle(
        davResource: DavResource,
        extSources: List<DavResource>,
        rootPath: String,
        header: Map<String, String>,
        history: PlayHistoryEntity?
    ): String? {
        //从播放记录读取字幕
        if (history?.subtitlePath != null) {
            return history.subtitlePath
        }

        //匹配同文件夹内同名字幕
        if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()) {
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

    private suspend fun matchAndDownloadDanmu(
        davResource: DavResource,
        rootPath: String,
        header: Map<String, String>
    ): Pair<String, Int>? {
        return withContext(Dispatchers.IO) {
            val url = rootPath + davResource.href.toASCIIString()
            var hash: String? = null
            try {
                //目标长度为前16M，17是容错
                val httpHelper = HashMap(header)
                httpHelper["range"] = "bytes=0-${17 * 1024 * 1024}"
                val responseBody = Retrofit.extService.downloadResource(url, header)
                hash = FileHashUtils.getHash(responseBody.byteStream())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (hash.isNullOrEmpty()) {
                return@withContext null
            }
            return@withContext DanmuUtils.matchDanmuSilence(davResource.name, hash)
        }
    }
}