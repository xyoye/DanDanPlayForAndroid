package com.xyoye.common_component.network.helper

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.RetryAndFollowUpInterceptor

/**
 * Created by xyoye on 2020/8/20.
 *
 * 处理网络请求重定向Authorization丢失的拦截器
 * OkHttp在处理重定向请求时, 当重定向的协议域名端口不一致，会移除Authorization请求头
 * @see RetryAndFollowUpInterceptor.buildRedirectRequest
 */
class RedirectAuthorizationInterceptor : Interceptor {
    companion object {
        private const val TAG_HEADER = "Authorization"
    }

    //重定向Authorization请求头缓存
    private val authorizationCache = mutableMapOf<String, String>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = considerUseCacheHeader(chain.request())
        return chain.proceed(request).also {
            considerAddCacheHeader(it)
        }
    }

    /**
     * 当请求链接在重定向请求头缓存中存在，且该请求不包含Authorization请求头
     * 在当前请求加上已缓存的Authorization请求头，并移除此缓存
     */
    private fun considerUseCacheHeader(request: Request): Request {
        val requestUrl = request.url.toString()
        val requestAuthorization = request.header(TAG_HEADER)
        if (authorizationCache.containsKey(requestUrl) && requestAuthorization == null) {
            val authorization = authorizationCache.remove(requestUrl)
                ?: return request

            return request.newBuilder().addHeader(TAG_HEADER, authorization).build()
        }
        return request
    }

    /**
     * 当响应结果为重定向结果时，如果该响应的请求的请求头包含Authorization
     * 缓存重定向地址与Authorization请求头内容
     */
    private fun considerAddCacheHeader(response: Response) {
        if (isRedirectResponse(response).not()) {
            return
        }

        val redirectUrl = response.header("Location")
        val authorization = response.request.header(TAG_HEADER)
        if (redirectUrl != null && authorization != null) {
            authorizationCache[redirectUrl] = authorization
        }
    }

    /**
     * HttpCode为301、302、303时，视为重定向请求
     */
    private fun isRedirectResponse(response: Response): Boolean {
        return response.code in 301..303
    }
}