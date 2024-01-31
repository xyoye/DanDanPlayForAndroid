package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.config.HeaderKey
import com.xyoye.common_component.storage.helper.ScreencastConstants
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

    @GET(ScreencastConstants.ReceiverApi.init)
    suspend fun init(
        @Header(HeaderKey.BASE_URL) url: String,
        @Header(HeaderKey.AUTHORIZATION) authorization: String?,
        @Header(ScreencastConstants.Header.versionKey) version: Int
    ): retrofit2.Response<CommonJsonData>

    @POST(ScreencastConstants.ReceiverApi.play)
    suspend fun play(
        @Header(HeaderKey.BASE_URL) url: String,
        @Header(HeaderKey.AUTHORIZATION) authorization: String?,
        @Body data: RequestBody
    ): CommonJsonData
}