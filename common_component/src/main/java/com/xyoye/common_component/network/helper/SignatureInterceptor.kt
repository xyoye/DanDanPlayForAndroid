package com.xyoye.common_component.network.helper

import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.SecurityHelper
import okhttp3.Interceptor
import okhttp3.Response


/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/1/21
 *    desc  : 签名验证拦截器
 */
class SignatureInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val newRequest = oldRequest.newBuilder()

        SecurityHelper.getInstance().getSignatureMap(
            oldRequest.url.encodedPath,
            BaseApplication.getAppContext()
        ).forEach {
            newRequest.addHeader(it.key, it.value ?: "")
        }

        return chain.proceed(newRequest.build())
    }
}