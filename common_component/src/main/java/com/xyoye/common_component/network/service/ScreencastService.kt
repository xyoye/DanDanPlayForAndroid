package com.xyoye.common_component.network.service

import com.xyoye.data_component.data.CommonJsonData
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/25
 *     desc  :
 * </pre>
 */

interface ScreencastService {

    @GET("/init")
    suspend fun init(
        @Header("host") host: String,
        @Header("port") port: Int,
        @Header("Authorization") authorization: String?
    ): CommonJsonData

    @POST("/play")
    suspend fun play(
        @Header("host") host: String,
        @Header("port") port: Int,
        @Header("Authorization") authorization: String?,
        @Body data: RequestBody
    ): CommonJsonData
}