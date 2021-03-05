package com.xyoye.common_component.network.service

import com.xyoye.data_component.data.MagnetResourceData
import com.xyoye.data_component.data.MagnetSubgroupData
import com.xyoye.data_component.data.MagnetTypeData
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by xyoye on 2020/8/19.
 */

interface ResRetrofitService {

    @GET("/list")
    suspend fun searchMagnet(
        @Query("keyword") keyword: String,
        @Query("type") typeId: String?,
        @Query("subgroup") subgroupId: String?
    ): MagnetResourceData

    @GET("/type")
    suspend fun getMagnetType(): MagnetTypeData

    @GET("/subgroup")
    suspend fun getMagnetSubgroup(): MagnetSubgroupData
}