package com.xyoye.cache

import com.danikula.videocache.HttpProxyCacheServer
import com.xyoye.common_component.base.app.BaseApplication


/**
 * Created by xyoye on 2021/12/23
 */
object CacheManager {

    private val headersInjector = VideoHeadersInjector()
    private var cacheServer: HttpProxyCacheServer? = null

    private fun createCacheServer(): HttpProxyCacheServer {
        val server = HttpProxyCacheServer.Builder(BaseApplication.getAppContext())
            .headerInjector(headersInjector)
            .sourceFactory(OkHttpUrlSourceFactory())
            .build()
        cacheServer = server
        return server
    }

    fun getCacheUrl(url: String, headers: Map<String, String>? = null): String {
        val server = cacheServer ?: createCacheServer()

        if (headers != null && headers.isNotEmpty()) {
            headersInjector.registerHeader(url, headers)
        }
        return server.getProxyUrl(url)
    }

    fun release() {
        cacheServer?.shutdown()
        cacheServer = null
    }
}