package com.xyoye.cache

import com.danikula.videocache.headers.HeaderInjector


/**
 * Created by xyoye on 2021/12/23
 */
class VideoHeadersInjector : HeaderInjector {
    private val headersMap = mutableMapOf<String, Map<String, String>>()

    override fun addHeaders(url: String?): Map<String, String> {
        if (url.isNullOrEmpty())
            return mutableMapOf()

        return headersMap[url]
            ?: return mutableMapOf()
    }

    fun registerHeader(url: String, headers: Map<String, String>) {
        headersMap[url] = headers
    }

    fun remove(url: String) {
        headersMap.remove(url)
    }

    fun clear() {
        headersMap.clear()
    }
}