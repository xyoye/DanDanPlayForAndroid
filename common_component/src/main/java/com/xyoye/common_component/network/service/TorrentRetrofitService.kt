package com.xyoye.common_component.network.service

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by xyoye on 2021/1/3.
 */

interface TorrentRetrofitService {

    @POST("/Magnet/Parse")
    suspend fun downloadTorrent(@Body requestBody: RequestBody): ResponseBody
}