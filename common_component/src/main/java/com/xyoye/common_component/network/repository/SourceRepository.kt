package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/6.
 */

object SourceRepository : BaseRepository() {

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
            Retrofit.danDanPlayService.matchDanmu(it)
        }

    /**
     * 搜索弹幕
     */
    suspend fun searchDanmu(anime: String) = request()
        .param("anime", anime)
        .doGet {
            Retrofit.danDanPlayService.searchDanmu(it)
        }

    /**
     * 获取弹幕内容
     */
    suspend fun getDanmuContent(episodeId: String, withRelated: Boolean = true) = request()
        .param("withRelated", withRelated)
        .doGet {
            Retrofit.danDanPlayService.getDanmuContent(episodeId)
        }

    /**
     * 获取第三方弹幕
     */
    suspend fun getRelatedDanmu(episodeId: String) = request()
        .doGet {
            Retrofit.danDanPlayService.getRelatedDanmu(episodeId)
        }

    /**
     * 获取第三方弹幕内容
     */
    suspend fun getRelatedDanmuContent(url: String) = request()
        .param("url", url)
        .doGet {
            Retrofit.danDanPlayService.getRelatedDanmuContent(it)
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
            Retrofit.danDanPlayService.sendOneDanmu(episodeId, it)
        }
}