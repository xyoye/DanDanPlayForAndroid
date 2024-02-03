package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.config.HeaderKey
import com.xyoye.common_component.network.request.RequestParams
import com.xyoye.data_component.data.MagnetResourceData
import com.xyoye.data_component.data.MagnetSubgroupData
import com.xyoye.data_component.data.MagnetTypeData
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap

/**
 * Created by xyoye on 2020/8/19.
 */

interface MagnetService {

    @GET("/list")
    suspend fun searchMagnet(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @QueryMap params: RequestParams
    ): MagnetResourceData

    @GET("/type")
    suspend fun getMagnetType(
        @Header(HeaderKey.BASE_URL) baseUrl: String
    ): MagnetTypeData

    @GET("/subgroup")
    suspend fun getMagnetSubgroup(
        @Header(HeaderKey.BASE_URL) baseUrl: String
    ): MagnetSubgroupData
}