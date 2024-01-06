package com.xyoye.common_component.network.request

import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.CommonJsonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Created by xyoye on 2024/1/5
 */

class Request {
    private val requestParams = hashMapOf<String, Any>()
    private var retry = 0

    fun param(key: String, value: Any): Request {
        requestParams[key] = value
        return this
    }

    fun params(map: Map<String, Any>): Request {
        requestParams.putAll(map)
        return this
    }

    fun retry(count: Int): Request {
        retry = count
        return this
    }

    suspend fun <T> doGet(
        api: suspend (Map<String, Any>) -> T
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
                if (considerRetry(e)) {
                    return@withContext doGet(api)
                }
                return@withContext Response.Error(RequestError.formException(e))
            }
        }
    }

    suspend fun <T> doPost(
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
                if (considerRetry(e)) {
                    return@withContext doPost(api)
                }
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

    private fun considerRetry(t: Throwable) = t is IOException && retry-- > 0
}