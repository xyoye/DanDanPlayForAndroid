package com.xyoye.common_component.network.helper

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by xyoye on 2021/3/28.
 */

class RemoteInterceptor private constructor() : Interceptor {

    var remoteUrl: String? = null
    var remoteToken: String? = null

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = RemoteInterceptor()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        var newRequest = oldRequest.newBuilder()
        //替换URL
        val newBaseUrl = remoteUrl?.run {
            HttpUrl.parse(this)
        }
        if (newBaseUrl != null) {
            val newUrl = oldRequest.url()
                .newBuilder()
                .scheme(newBaseUrl.scheme())
                .host(newBaseUrl.host())
                .port(newBaseUrl.port())
                .build()
            newRequest = newRequest.url(newUrl)
            //设置密钥
            if (!remoteToken.isNullOrEmpty()) {
                newRequest.header("Authorization", "Bearer $remoteToken")
            }
            return chain.proceed(newRequest.build())
        }
        return chain.proceed(oldRequest)
    }
}