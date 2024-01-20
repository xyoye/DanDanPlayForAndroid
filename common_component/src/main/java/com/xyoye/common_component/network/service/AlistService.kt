package com.xyoye.common_component.network.service

import com.xyoye.common_component.network.config.HeaderKey
import com.xyoye.data_component.data.CommonJsonModel
import com.xyoye.data_component.data.alist.AlistDirectoryData
import com.xyoye.data_component.data.alist.AlistFileData
import com.xyoye.data_component.data.alist.AlistLoginData
import com.xyoye.data_component.data.alist.AlistRootData
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by xyoye on 2024/1/20.
 */

interface AlistService {

    @POST("/api/auth/login")
    suspend fun login(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @Body requestBody: RequestBody
    ): CommonJsonModel<AlistLoginData>

    @GET("/api/me")
    suspend fun getRootPath(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @Header(HeaderKey.AUTHORIZATION) authorization: String?,
    ): CommonJsonModel<AlistRootData>

    @POST("/api/fs/list")
    suspend fun openDirectory(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @Header(HeaderKey.AUTHORIZATION) authorization: String?,
        @Body requestBody: RequestBody
    ): CommonJsonModel<AlistDirectoryData>

    @POST("/api/fs/get")
    suspend fun openFile(
        @Header(HeaderKey.BASE_URL) baseUrl: String,
        @Header(HeaderKey.AUTHORIZATION) authorization: String?,
        @Body requestBody: RequestBody
    ): CommonJsonModel<AlistFileData>
}