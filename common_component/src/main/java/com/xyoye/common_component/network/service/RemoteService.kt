package com.xyoye.common_component.network.service

import com.xyoye.data_component.data.remote.RemotePlayInfo
import com.xyoye.data_component.data.remote.RemoteSubtitleData
import com.xyoye.data_component.data.remote.RemoteVideoData
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by xyoye on 2021/3/28.
 */

interface RemoteService {

    @GET("/api/v1/playlist")
    suspend fun test(): ResponseBody

    @GET("/api/v1/library")
    suspend fun openStorage(): List<RemoteVideoData>

    @GET("/api/v1/comment/{hash}")
    suspend fun downloadDanmu(@Path("hash") hash: String): ResponseBody

    @GET("/api/v1/subtitle/info/{id}")
    suspend fun searchSubtitle(@Path("id") id: String): RemoteSubtitleData

    @GET("/api/v1/subtitle/file/{id}")
    suspend fun downloadSubtitle(
        @Path("id") id: String,
        @Query("fileName") fileName: String
    ): ResponseBody

    @GET("/api/v1/current/video")
    suspend fun getPlayInfo(): RemotePlayInfo

    @GET("/api/v1/control/{method}")
    suspend fun control(@Path("method") method: String)

    @GET("/api/v1/control/volume/{volume}")
    suspend fun volume(@Path("volume") volume: String)

    @GET("/api/v1/control/seek/{time}")
    suspend fun seek(@Path("time") time: String)
}