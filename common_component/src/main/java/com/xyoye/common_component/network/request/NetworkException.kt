package com.xyoye.common_component.network.request

import android.util.MalformedJsonException
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.data.CommonJsonModel
import kotlinx.serialization.SerializationException
import org.json.JSONException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.cancellation.CancellationException

class NetworkException(
    val code: Int,
    msg: String,
    cause: Exception?
) : Throwable("x$code $msg", cause) {

    companion object {

        fun formJsonData(data: CommonJsonData) = NetworkException(
            data.errorCode,
            data.errorMessage ?: "服务端处理失败",
            IllegalStateException()
        )

        fun formJsonModel(data: CommonJsonModel<*>) = NetworkException(
            data.code,
            data.message.ifEmpty { "服务端处理失败" },
            IllegalStateException()
        )

        fun formException(e: Exception): NetworkException {
            return when (e) {
                is HttpException -> formHttpException(e)

                is UnknownHostException -> 1001 to "网络错误"


                is TimeoutException -> 1002 to "网络连接超时"


                is SocketTimeoutException -> 1003 to "网络请求超时"


                is SSLHandshakeException -> 1004 to "证书验证失败"


                is SerializationException -> 1005 to "响应数据类型匹配失败"


                is JSONException -> 1006 to "解析响应数据错误"


                is ParseException -> 1007 to "解析响应数据错误"


                is MalformedJsonException -> 1008 to "解析响应数据错误"


                is CancellationException -> 2000 to "网络请求中断"

                else -> -1 to "${e.message}"

            }.run { NetworkException(first, second, e) }
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
