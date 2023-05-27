package com.xyoye.common_component.network.helper

import android.net.Uri
import com.xyoye.common_component.config.AppConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by xyoye on 2023/5/27.
 */

class BackupDomainInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (AppConfig.isBackupDomainEnable().not()) {
            return chain.proceed(chain.request())
        }

        val oldRequest = chain.request()
        val newRequest = oldRequest.newBuilder()
        val newBaseUrl = Uri.parse(AppConfig.getBackupDomain())
        val newUrl = oldRequest.url
            .newBuilder()
            .scheme(newBaseUrl.scheme ?: "")
            .host(newBaseUrl.host ?: "")
            .port(newBaseUrl.port)
            .build()
        return chain.proceed(newRequest.url(newUrl).build())
    }
}