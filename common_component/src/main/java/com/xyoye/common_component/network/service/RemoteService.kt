package com.xyoye.common_component.network.service

import com.xyoye.data_component.data.remote.RemoteVideoData
import okhttp3.ResponseBody
import retrofit2.http.GET

/**
 * Created by xyoye on 2021/3/28.
 */

interface RemoteService {

    @GET("/api/v1/playlist")
    suspend fun test(): ResponseBody

    @GET("/api/v1/library")
    suspend fun openStorage(): List<RemoteVideoData>
}