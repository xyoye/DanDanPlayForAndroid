package com.xyoye.common_component.network.service

import com.xyoye.data_component.data.*
import retrofit2.http.*

/**
 * Created by xyoye on 2020/7/6.
 */

interface RetrofitService {
    @GET("api/v2/bangumi/shin")
    suspend fun getWeeklyAnime(@Query("filterAdultContent") filterAdultContent: Boolean = true): BangumiAnimeData

    @GET("api/v2/homepage/banner")
    suspend fun getBanners(): BannerData

    @GET("api/v2/bangumi/{animeId}")
    suspend fun getAnimeDetail(@Path("animeId") animeId: String): AnimeDetailData

    @FormUrlEncoded
    @POST("/api/v2/favorite")
    suspend fun follow(@FieldMap params: Map<String, String>): CommonJsonData

    @DELETE("/api/v2/favorite/{animeId}")
    suspend fun unFollow(@Path("animeId") animeId: String): CommonJsonData

    @GET("api/v2/favorite")
    suspend fun getFollowAnime(@Query("onlyOnAir") onlyOnAir: Boolean = false): FollowAnimeData

    @GET("/api/v2/search/anime")
    suspend fun searchAnime(
        @Query("keyword") keyword: String,
        @Query("type") type: String?
    ): SearchAnimeData

    @GET("/api/v2/bangumi/season/anime/{year}/{month}")
    suspend fun getSeasonAnime(
        @Path("year") year: String,
        @Path("month") month: String
    ): BangumiAnimeData

    @FormUrlEncoded
    @POST("/api/v2/match")
    suspend fun matchDanmu(@FieldMap params: Map<String, String>): DanmuMatchData

    @GET("/api/v2/search/tag")
    suspend fun searchByTag(@Query("tags") tagId: String): AnimeTagData

    @GET("/api/v2/search/episodes")
    suspend fun searchDanmu(
        @Query("anime") anime: String,
        @Query("episode") episode: String
    ): DanmuSearchData

    @GET("api/v2/comment/{episodeId}")
    @Headers("accept-encoding: gzip")
    suspend fun getDanmuContent(
        @Path("episodeId") episodeId: String,
        @Query("withRelated") withRelated: Boolean = false,
        @Query("chConvert") format: Int = 0
    ): DanmuData

    @FormUrlEncoded
    @POST("api/v2/comment/{episodeId}")
    suspend fun sendDanmu(
        @Path("episodeId") episodeId: String,
        @FieldMap params: Map<String, String>
    ): SendDanmuData

    @GET("/api/v2/related/{episodeId}")
    suspend fun getDanmuRelated(@Path("episodeId") episodeId: String): DanmuRelatedData

    @GET("/api/v2/extcomment")
    @Headers("accept-encoding: gzip")
    suspend fun getDanmuExtContent(@Query("url") url: String): DanmuData

    @FormUrlEncoded
    @POST("api/v2/login")
    suspend fun login(@FieldMap params: Map<String, String>): LoginData

    @GET("api/v2/login/renew")
    suspend fun reLogin(): LoginData

    @FormUrlEncoded
    @POST("api/v2/register")
    suspend fun register(@FieldMap params: Map<String, String>): LoginData

    @FormUrlEncoded
    @POST("api/v2/register/resetpassword")
    suspend fun resetPassword(@FieldMap params: Map<String, String>): CommonJsonData

    @FormUrlEncoded
    @POST("api/v2/register/findmyid")
    suspend fun retrieveAccount(@FieldMap params: Map<String, String>): CommonJsonData

    @GET("api/v2/playhistory")
    suspend fun getCloudHistory(): CloudHistoryListData

    @FormUrlEncoded
    @POST("/api/v2/user/profile")
    suspend fun updateScreenName(@FieldMap params: Map<String, String>): CommonJsonData

    @FormUrlEncoded
    @POST("/api/v2/user/password")
    suspend fun updatePassword(@FieldMap params: Map<String, String>): CommonJsonData
}