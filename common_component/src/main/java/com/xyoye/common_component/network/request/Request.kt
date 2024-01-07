package com.xyoye.common_component.network.request

import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.CommonJsonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Created by xyoye on 2024/1/5
 */

class Request {
    private val requestParams: RequestParams = hashMapOf()

    fun param(key: String, value: Any?): Request {
        value ?: return this

        requestParams[key] = value
        return this
    }

    fun params(map: Map<String, Any>): Request {
        requestParams.putAll(map)
        return this
    }

    suspend fun <T: Any> doDelete(
        api: suspend (RequestParams) -> T
    ): Response<T> {
        return doGet(api)
    }

    suspend fun <T: Any> doGet(
        api: suspend (RequestParams) -> T
    ): Response<T> {
        return withContext(Dispatchers.IO) {
            try {
                val result = api.invoke(requestParams)
                if (result is CommonJsonData && result.success.not()) {
                    return@withContext Response.Error(RequestError.formJsonData(result))
                }

                return@withContext Response.Success(result)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Response.Error(RequestError.formException(e))
            }
        }
    }

    suspend fun <T: Any> doPost(
        api: suspend (RequestBody) -> T
    ): Response<T> {
        return withContext(Dispatchers.IO) {
            try {
                val result = api.invoke(requestBody())
                if (result is CommonJsonData && result.success.not()) {
                    return@withContext Response.Error(RequestError.formJsonData(result))
                }

                return@withContext Response.Success(result)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Response.Error(RequestError.formException(e))
            }
        }
    }

    /**
     * Post请求体
     */
    private fun requestBody(): RequestBody {
        val params: Map<String, Any> = requestParams
        val requestJson = JsonHelper.toJson(params).orEmpty()
        val mediaType = "application/json;charset=utf-8".toMediaType()
        return requestJson.toRequestBody(mediaType)
    }
}