package com.xyoye.common_component.network.helper

import com.xyoye.common_component.network.config.HeaderKey
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by xyoye on 2021/3/28.
 */

class DynamicBaseUrlInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val baseUrl = request.header(HeaderKey.BASE_URL)
        if (baseUrl.isNullOrEmpty()) {
            return chain.proceed(request)
        }

        val baseHttpUrl = baseUrl.toHttpUrlOrNull()
            ?: return chain.proceed(request)

        val newUrl = request.url
            .newBuilder()
            .scheme(baseHttpUrl.scheme)
            .host(baseHttpUrl.host)
            .port(baseHttpUrl.port)
            .build()

        val newRequest = request.newBuilder()
            .removeHeader(HeaderKey.BASE_URL)
            .url(newUrl)
            .build()
        return chain.proceed(newRequest)
    }
}