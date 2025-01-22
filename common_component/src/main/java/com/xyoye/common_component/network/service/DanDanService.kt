package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.helper.DeveloperCertificateInterceptor
import com.xyoye.common_component.network.request.RequestParams
import com.xyoye.data_component.data.AnimeDetailData
import com.xyoye.data_component.data.AnimeTagData
import com.xyoye.data_component.data.BangumiAnimeData
import com.xyoye.data_component.data.BannerData
import com.xyoye.data_component.data.CloudHistoryListData
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.data.DanmuData
import com.xyoye.data_component.data.DanmuMatchData
import com.xyoye.data_component.data.DanmuRelatedData
import com.xyoye.data_component.data.DanmuSearchData
import com.xyoye.data_component.data.FollowAnimeData
import com.xyoye.data_component.data.LoginData
import com.xyoye.data_component.data.SearchAnimeData
import com.xyoye.data_component.data.SendDanmuData
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * Created by xyoye on 2024/1/6.
 */

interface DanDanService {

    @GET("api/v2/bangumi/shin")
    suspend fun getWeeklyAnime(@QueryMap params: RequestParams): BangumiAnimeData

    @GET("api/v2/homepage/banner")
    suspend fun getHomeBanner(): BannerData

    @GET("api/v2/bangumi/{animeId}")
    suspend fun getAnimeDetail(@Path("animeId") animeId: String): AnimeDetailData

    @POST("/api/v2/favorite")
    suspend fun followAnime(@Body body: RequestBody): CommonJsonData

    @DELETE("/api/v2/favorite/{animeId}")
    suspend fun cancelFollowAnime(@Path("animeId") animeId: String): CommonJsonData

    @GET("api/v2/favorite")
    suspend fun getFollowedAnime(@QueryMap params: RequestParams): FollowAnimeData

    @GET("/api/v2/search/anime")
    suspend fun searchAnime(@QueryMap params: RequestParams): SearchAnimeData

    @GET("/api/v2/search/tag")
    suspend fun searchAnimeByTag(@QueryMap params: RequestParams): AnimeTagData

    @GET("/api/v2/bangumi/season/anime/{year}/{month}")
    suspend fun getSeasonAnime(
        @Path("year") year: String,
        @Path("month") month: String
    ): BangumiAnimeData

    @POST("/api/v2/match")
    suspend fun matchDanmu(@Body body: RequestBody): DanmuMatchData

    @GET("/api/v2/search/episodes")
    suspend fun searchDanmu(@QueryMap params: RequestParams): DanmuSearchData

    @GET("api/v2/comment/{episodeId}")
    @Headers("accept-encoding: gzip")
    suspend fun getDanmuContent(
        @Path("episodeId") episodeId: String,
        @QueryMap params: RequestParams
    ): DanmuData

    @POST("api/v2/comment/{episodeId}")
    suspend fun sendOneDanmu(
        @Path("episodeId") episodeId: String,
        @Body body: RequestBody
    ): SendDanmuData

    @GET("/api/v2/related/{episodeId}")
    suspend fun getRelatedDanmu(@Path("episodeId") episodeId: String): DanmuRelatedData

    @GET("/api/v2/extcomment")
    @Headers("accept-encoding: gzip")
    suspend fun getRelatedDanmuContent(@QueryMap params: RequestParams): DanmuData

    @POST("api/v2/login")
    suspend fun login(@Body body: RequestBody): LoginData

    @GET("api/v2/login/renew")
    suspend fun refreshToken(): LoginData

    @POST("api/v2/register")
    suspend fun register(@Body body: RequestBody): LoginData

    @POST("api/v2/register/resetpassword")
    suspend fun resetPassword(@Body body: RequestBody): CommonJsonData

    @POST("api/v2/register/findmyid")
    suspend fun retrieveAccount(@Body body: RequestBody): CommonJsonData

    @GET("api/v2/playhistory")
    suspend fun getPlayHistory(): CloudHistoryListData

    @POST("/api/v2/user/profile")
    suspend fun updateScreenName(@Body body: RequestBody): CommonJsonData

    @POST("/api/v2/user/password")
    suspend fun updatePassword(@Body body: RequestBody): CommonJsonData

    @GET("/config/filter.xml")
    suspend fun getCloudFilters(): ResponseBody

    @POST("/api/v2/playhistory")
    suspend fun addPlayHistory(@Body body: RequestBody): CommonJsonData

    @GET("api/v2/login/renew")
    suspend fun checkAuthenticate(
        @Header(DeveloperCertificateInterceptor.HEADER_APP_ID) appId: String,
        @Header(DeveloperCertificateInterceptor.HEADER_APP_SECRET) appSecret: String,
        @Header("X-Auth") authMode: Int
    ): Response<ResponseBody>
}