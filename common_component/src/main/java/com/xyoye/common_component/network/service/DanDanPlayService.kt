package com.xyoye.common_component.network.service

import com.xyoye.data_component.data.BangumiAnimeData
import com.xyoye.data_component.data.DanmuMatchData
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

/**
 * Created by xyoye on 2024/1/6.
 */

interface DanDanPlayService {

    @GET("api/v2/bangumi/shin")
    suspend fun getWeeklyAnime(@QueryMap params: Map<String, @JvmSuppressWildcards Any>): BangumiAnimeData

    @POST("/api/v2/match")
    suspend fun matchDanmu(@Body body: RequestBody): DanmuMatchData
}