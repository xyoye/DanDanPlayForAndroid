package com.xyoye.common_component.network.helper

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
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val host = oldRequest.header("host")
        val port = oldRequest.header("port")

        val newRequest = oldRequest.newBuilder()
        if (host != null && port != null) {
            newRequest.removeHeader("host")
            newRequest.removeHeader("port")

            val newUrl = oldRequest.url()
                .newBuilder()
                .host(host)
                .port(port.toIntOrNull() ?: 0)
                .build()
            return chain.proceed(newRequest.url(newUrl).build())
        }
        return chain.proceed(oldRequest)
    }
}