package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/5
 */

object DanDanPlayRepository : BaseRepository() {

    suspend fun getWeeklyAnime() = request()
        .param("filterAdultContent", true)
        .doGet {
            Retrofit.danDanPlayService.getWeeklyAnime(it)
        }

    suspend fun matchDanmu(hash: String) = request()
        .param("fileHash", hash)
        .param("fileName", "empty")
        .param("fileSize", 0)
        .param("videoDuration", 0)
        .param("matchMode", "hashOnly")
        .doPost {
            Retrofit.danDanPlayService.matchDanmu(it)
        }
}