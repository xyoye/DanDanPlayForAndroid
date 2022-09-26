package com.xyoye.common_component.network.helper

import android.util.Base64
import com.xyoye.common_component.utils.EntropyUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/25
 *     desc  :
 * </pre>
 */

class ScreencastInterceptor : Interceptor {
    companion object {
        const val KEY_AUTHORIZATION = "IiHcoJPwt5TCrR2r"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val host = oldRequest.header("host")
        val port = oldRequest.header("port")

        val oldAuth = oldRequest.header("Authorization")
        val newAuth = createAuthorization(oldAuth)

        val newRequest = oldRequest.newBuilder()
        if (host != null && port != null) {
            newRequest.apply {
                removeHeader("host")
                removeHeader("port")
                if (newAuth != null) {
                    removeHeader("Authorization")
                    addHeader("Authorization", newAuth)
                }
            }


            val newUrl = oldRequest.url()
                .newBuilder()
                .host(host)
                .port(port.toIntOrNull() ?: 0)
                .build()
            return chain.proceed(newRequest.url(newUrl).build())
        }
        return chain.proceed(oldRequest)
    }

    private fun createAuthorization(password: String?): String? {
        if (password == null || password.isEmpty()) {
            return null
        }

        val authorization = EntropyUtils.aesEncode(
            KEY_AUTHORIZATION,
            password,
            Base64.NO_WRAP
        )
        if (authorization == null || authorization.isEmpty()) {
            return null
        }
        return "Bearer $authorization"
    }
}