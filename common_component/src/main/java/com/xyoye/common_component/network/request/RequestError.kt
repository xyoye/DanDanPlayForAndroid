package com.xyoye.common_component.network.request

import android.util.MalformedJsonException
import com.squareup.moshi.JsonDataException
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.data.CommonJsonModel
import org.json.JSONException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.cancellation.CancellationException

data class RequestError(
    val code: Int,
    val msg: String,
    val original: Exception
) : Throwable() {

    val toastMsg: String get() = "x$code $msg"

    companion object {

        fun formJsonData(data: CommonJsonData): RequestError {
            val message = data.errorMessage ?: "服务端处理失败"
            return RequestError(
                data.errorCode,
                message,
                IllegalStateException(message)
            )
        }

        fun formJsonModel(data: CommonJsonModel<*>): RequestError {
            val message = data.message.ifEmpty { "服务端处理失败" }
            return RequestError(
                data.code,
                message,
                IllegalStateException(message)
            )
        }

        fun formException(e: Exception): RequestError {
            return when (e) {
                is HttpException -> formHttpException(e)

                is UnknownHostException -> 1001 to "网络错误"


                is TimeoutException -> 1002 to "网络连接超时"


                is SocketTimeoutException -> 1003 to "网络请求超时"


                is SSLHandshakeException -> 1004 to "证书验证失败"


                is JsonDataException -> 1005 to "响应数据类型匹配失败"


                is JSONException -> 1006 to "解析响应数据错误"


                is ParseException -> 1007 to "解析响应数据错误"


                is MalformedJsonException -> 1008 to "解析响应数据错误"


                is CancellationException -> 2000 to "网络请求中断"

                else -> -1 to "${e.message}"

            }.run { RequestError(first, second, e) }
        }

        private fun formHttpException(e: HttpException): Pair<Int, String> {
            return when (e.code()) {
                401 -> "操作未授权"

                403 -> "请求被拒绝"


                404 -> "资源不存在"


                405 -> "缺少参数"


                408 -> "服务器执行超时"


                500 -> "服务器内部错误"


                503 -> "服务器不可用"


                else -> "网络错误"

            }.run { e.code() to this }
        }
    }
}
