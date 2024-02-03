package com.xyoye.common_component.network.helper

import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.extension.authorizationValue
import com.xyoye.common_component.network.config.HeaderKey
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by xyoye on 2020/8/20.
 */

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header(HeaderKey.AUTHORIZATION, UserConfig.getUserToken()?.authorizationValue().orEmpty())
                .build()
        )
    }
}