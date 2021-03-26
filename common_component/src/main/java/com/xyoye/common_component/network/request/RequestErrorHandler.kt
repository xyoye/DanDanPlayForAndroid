package com.xyoye.common_component.network.request

import android.util.MalformedJsonException
import com.squareup.moshi.JsonDataException
import com.xyoye.data_component.data.CommonJsonData
import org.json.JSONException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

/**
 * Created by xyoye on 2020/7/6.
 */

class RequestErrorHandler(private val e: java.lang.Exception) {

    companion object {
        fun handleCommonError(jsonData: CommonJsonData) = RequestError(
            jsonData.errorCode,
            jsonData.errorMessage ?: "服务端处理失败"
        )
    }

    fun handlerError(): RequestError {
        e.printStackTrace()
        return when (e) {
            is HttpException -> handleHttpException(e)

            is UnknownHostException -> RequestError(
                1001,
                "网络错误"
            )

            is TimeoutException -> RequestError(
                1002,
                "网络连接超时"
            )

            is SocketTimeoutException -> RequestError(
                1003,
                "网络请求超时"
            )

            is SSLHandshakeException -> RequestError(
                1004,
                "证书验证失败"
            )

            is JsonDataException -> RequestError(
                1005,
                "响应数据类型匹配失败"
            )
            is JSONException -> RequestError(
                1006,
                "解析响应数据错误"
            )
            is ParseException -> RequestError(
                1007,
                "解析响应数据错误"
            )
            is MalformedJsonException -> RequestError(
                1008,
                "解析响应数据错误"
            )

            else -> RequestError(
                -1,
                "其它错误: ${e.message}"
            )
        }
    }

    private fun handleHttpException(e: HttpException): RequestError {
        return when (e.code()) {
            401 -> RequestError(
                401,
                "操作未授权"
            )
            403 -> RequestError(
                403,
                "请求被拒绝"
            )
            404 -> RequestError(
                404,
                "资源不存在"
            )
            405 -> RequestError(
                405,
                "缺少参数"
            )
            408 -> RequestError(
                408,
                "服务器执行超时"
            )
            500 -> RequestError(
                500,
                "服务器内部错误"
            )
            503 -> RequestError(
                503,
                "服务器不可用"
            )
            else -> RequestError(
                e.code(),
                "网络错误"
            )
        }
    }
}