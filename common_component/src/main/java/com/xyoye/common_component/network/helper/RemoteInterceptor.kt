package com.xyoye.common_component.network.helper

import com.xyoye.common_component.utils.RemoteHelper
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by xyoye on 2021/3/28.
 */

class RemoteInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        var newRequest = oldRequest.newBuilder()
        //替换URL
        val newBaseUrl = RemoteHelper.getInstance().remoteUrl?.toHttpUrlOrNull()
        if (newBaseUrl != null) {
            val newUrl = oldRequest.url
                .newBuilder()
                .scheme(newBaseUrl.scheme)
                .host(newBaseUrl.host)
                .port(newBaseUrl.port)
                .build()
            newRequest = newRequest.url(newUrl)
            //设置密钥
            val remoteToken = RemoteHelper.getInstance().remoteToken
            if (!remoteToken.isNullOrEmpty()) {
                newRequest.header("Authorization", "Bearer $remoteToken")
            }
            return chain.proceed(newRequest.build())
        }
        return chain.proceed(oldRequest)
    }
}