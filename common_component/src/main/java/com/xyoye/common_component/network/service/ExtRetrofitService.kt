package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.config.Api
import com.xyoye.common_component.network.request.RequestParams
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

    @GET("${Api.THUNDER_SUB}{hash}.json")
    suspend fun matchSubtitleFormThunder(@Path("hash") hash: String): SubtitleThunderData

    @POST(Api.SHOOTER_SUB)
    suspend fun matchSubtitleFormShooter(@Body body: RequestBody): List<SubtitleShooterData>

    @GET("${Api.ASSRT_SUB}v1/sub/search")
    suspend fun searchSubtitle(@QueryMap params: RequestParams): SubtitleSubData

    @GET("${Api.ASSRT_SUB}v1/sub/detail")
    suspend fun searchSubtitleDetail(@QueryMap params: RequestParams): SubtitleSubData

    @GET
    @Streaming
    suspend fun getResourceResponse(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): retrofit2.Response<ResponseBody>

    @GET
    @Streaming
    suspend fun getResourceResponseBody(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): ResponseBody

    @GET("${Api.BILI_BILI}x/web-interface/view")
    @Streaming
    suspend fun getCidInfo(@QueryMap params: RequestParams): BiliBiliCidData

    @POST("${Api.HAN_LP}/api/parse")
    suspend fun segmentWords(@Body body: RequestBody): retrofit2.Response<ResponseBody>
}