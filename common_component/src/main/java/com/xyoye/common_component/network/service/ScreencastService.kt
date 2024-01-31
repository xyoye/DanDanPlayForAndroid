package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.config.HeaderKey
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
        @Header(HeaderKey.BASE_URL) url: String,
        @Header(HeaderKey.AUTHORIZATION) authorization: String?,
        @Header(HeaderKey.SCREENCAST_VERSION) version: Int
    ): retrofit2.Response<CommonJsonData>

    @POST("/play")
    suspend fun play(
        @Header(HeaderKey.BASE_URL) url: String,
        @Header(HeaderKey.AUTHORIZATION) authorization: String?,
        @Body data: RequestBody
    ): CommonJsonData
}