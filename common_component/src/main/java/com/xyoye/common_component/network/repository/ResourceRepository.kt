package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/6.
 */

object ResourceRepository : BaseRepository() {

    /**
     * 匹配弹幕
     */
    suspend fun matchDanmu(hash: String) = request()
        .param("fileHash", hash)
        .param("fileName", "empty")
        .param("fileSize", 0)
        .param("videoDuration", 0)
        .param("matchMode", "hashOnly")
        .doPost {
            Retrofit.danDanService.matchDanmu(it)
        }

    /**
     * 搜索弹幕
     */
    suspend fun searchDanmu(anime: String) = request()
        .param("anime", anime)
        .doGet {
            Retrofit.danDanService.searchDanmu(it)
        }

    /**
     * 获取弹幕内容
     */
    suspend fun getDanmuContent(episodeId: String, withRelated: Boolean = true) = request()
        .param("withRelated", withRelated)
        .doGet {
            Retrofit.danDanService.getDanmuContent(episodeId, it)
        }

    /**
     * 获取第三方弹幕
     */
    suspend fun getRelatedDanmu(episodeId: String) = request()
        .doGet {
            Retrofit.danDanService.getRelatedDanmu(episodeId)
        }

    /**
     * 获取第三方弹幕内容
     */
    suspend fun getRelatedDanmuContent(url: String) = request()
        .param("url", url)
        .doGet {
            Retrofit.danDanService.getRelatedDanmuContent(it)
        }

    /**
     * 发送一条弹幕
     */
    suspend fun sendOneDanmu(
        episodeId: String,
        time: String,
        mode: Int,
        color: Int,
        comment: String
    ) = request()
        .param("time", time)
        .param("mode", mode)
        .param("color", color)
        .param("comment", comment)
        .doPost {
            Retrofit.danDanService.sendOneDanmu(episodeId, it)
        }

    /**
     * 匹配字幕，Thunder
     */
    suspend fun matchSubtitleFormThunder(hash: String) = request()
        .doGet {
            Retrofit.extendedService.matchSubtitleFormThunder(hash)
        }

    /**
     * 匹配字幕，Shooter
     */
    suspend fun matchSubtitleFormShooter(fileHash: String, fileName: String) = request()
        .param("filehash", fileHash)
        .param("pathinfo", fileName)
        .param("format", "json")
        .param("lang", "Chn")
        .doPost {
            Retrofit.extendedService.matchSubtitleFormShooter(it)
        }

    /**
     * 搜索字幕
     */
    suspend fun searchSubtitle(
        token: String,
        keyword: String,
        page: Int
    ) = request()
        .param("token", token)
        .param("q", keyword)
        .param("pos", page)
        .doGet {
            Retrofit.extendedService.searchSubtitle(it)
        }

    /**
     * 字幕详情
     */
    suspend fun getSubtitleDetail(token: String, id: String) = request()
        .param("token", token)
        .param("id", id)
        .doGet {
            Retrofit.extendedService.searchSubtitleDetail(it)
        }

    /**
     * 获取资源响应
     */
    suspend fun getResourceResponse(url: String, headers: Map<String, String> = emptyMap()) = request()
        .doGet {
            Retrofit.extendedService.getResourceResponse(url, headers)
        }

    /**
     * 获取资源响应正文
     */
    suspend fun getResourceResponseBody(url: String, headers: Map<String, String> = emptyMap()) = request()
        .doGet {
            Retrofit.extendedService.getResourceResponseBody(url, headers)
        }
}