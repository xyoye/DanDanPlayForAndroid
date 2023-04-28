package com.xyoye.common_component.network.helper

import com.xyoye.common_component.config.AppConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response


/**
 * Created by XYJ on 2021/2/13.
 */

class ResDomainInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val newRequest = oldRequest.newBuilder()
        val newBaseUrl = AppConfig.getMagnetResDomain()?.toHttpUrlOrNull()
        if (newBaseUrl != null) {
            val newUrl = oldRequest.url
                .newBuilder()
                .scheme(newBaseUrl.scheme)
                .host(newBaseUrl.host)
                .port(newBaseUrl.port)
                .build()
            return chain.proceed(newRequest.url(newUrl).build())
        }
        return chain.proceed(oldRequest)
    }
}