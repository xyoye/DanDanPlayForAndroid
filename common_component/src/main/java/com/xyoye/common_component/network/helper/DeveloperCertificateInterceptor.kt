package com.xyoye.common_component.network.helper

import com.xyoye.common_component.config.DevelopConfig
import com.xyoye.common_component.utils.SecurityHelper
import okhttp3.Interceptor
import okhttp3.Response

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/1/22
 *    desc  : 开发者凭证拦截器
 */
class DeveloperCertificateInterceptor : Interceptor {
    companion object {
        const val HEADER_APP_ID = "X-AppId"
        const val HEADER_APP_SECRET = "X-AppSecret"
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()

        // 官方应用，不做处理
        if (SecurityHelper.getInstance().isOfficialApplication) {
            return chain.proceed(oldRequest)
        }

        // 请求自带凭证，不做处理
        val requestAppId = oldRequest.header(HEADER_APP_ID)
        val requestAppSecret = oldRequest.header(HEADER_APP_SECRET)
        if (requestAppId?.isNotEmpty() == true && requestAppSecret?.isNotEmpty() == true) {
            return chain.proceed(oldRequest)
        }

        // 未配置凭证，不做处理
        val appId = DevelopConfig.getAppId()
        val appSecret = DevelopConfig.getAppSecret()
        if (appId.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
            return chain.proceed(oldRequest)
        }

        // 添加凭证
        return chain.proceed(
            oldRequest.newBuilder()
                .header(HEADER_APP_ID, appId)
                .header(HEADER_APP_SECRET, appSecret).build()
        )
    }
}