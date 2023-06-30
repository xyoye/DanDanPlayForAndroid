package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.Retrofit
import com.xyoye.data_component.data.BiliBiliCidData
import com.xyoye.data_component.data.SubtitleShooterData
import com.xyoye.data_component.data.SubtitleSubData
import com.xyoye.data_component.data.SubtitleThunderData
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Created by xyoye on 2020/11/30.
 */

interface ExtRetrofitService {

    @GET
    suspend fun matchThunderSubtitle(@Url url: String): SubtitleThunderData?

    @FormUrlEncoded
    @POST
    suspend fun matchShooterSubtitle(
        @Url url: String,
        @FieldMap map: Map<String, String>
    ): MutableList<SubtitleShooterData>?

    @GET("v1/sub/search")
    suspend fun searchSubtitle(
        @Query("token") token: String,
        @Query("q") keyword: String,
        @Query("pos") page: Int
    ): SubtitleSubData

    @GET("v1/sub/detail")
    suspend fun searchSubtitleDetail(
        @Query("token") token: String,
        @Query("id") id: String
    ): SubtitleSubData

    @GET
    @Streaming
    suspend fun downloadResource(
        @Url url: String,
        @HeaderMap header: Map<String, String> = mapOf()
    ): ResponseBody

    @GET
    @Streaming
    suspend fun downloadResourceWithHeader(
        @Url url: String,
        @HeaderMap header: Map<String, String> = mapOf()
    ): retrofit2.Response<ResponseBody>

    @GET
    @Streaming
    suspend fun getCidInfo(@Url url: String): BiliBiliCidData

    @POST
    suspend fun segmentWords(
        @Url url: String = "${Retrofit.handLPUrl}/api/parse",
        @Body params: RequestBody
    ): retrofit2.Response<ResponseBody>
}