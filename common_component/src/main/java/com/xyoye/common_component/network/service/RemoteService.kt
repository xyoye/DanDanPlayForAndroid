package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.config.HeaderKey
import com.xyoye.common_component.network.request.RequestParams
import com.xyoye.data_component.data.remote.RemoteSubtitleData
import com.xyoye.data_component.data.remote.RemoteVideoData
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * Created by xyoye on 2021/3/28.
 */

interface RemoteService {

    @GET("/api/v1/library")
    suspend fun getStorageFiles(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @QueryMap params: RequestParams
    ): List<RemoteVideoData>

    @GET("/api/v1/comment/id/{id}")
    suspend fun downloadDanmu(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @Path("id") videoId: String,
        @QueryMap params: RequestParams
    ): ResponseBody

    @GET("/api/v1/subtitle/info/{id}")
    suspend fun getRelatedSubtitles(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @Path("id") videoId: String,
        @QueryMap params: RequestParams
    ): RemoteSubtitleData

    @GET("/api/v1/subtitle/file/{id}")
    suspend fun downloadSubtitle(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @Path("id") videoId: String,
        @QueryMap params: RequestParams
    ): ResponseBody
}