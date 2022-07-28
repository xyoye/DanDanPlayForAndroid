package com.xyoye.common_component.network.service

import com.xyoye.data_component.data.CommonJsonData
import retrofit2.http.GET
import retrofit2.http.Header

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
}