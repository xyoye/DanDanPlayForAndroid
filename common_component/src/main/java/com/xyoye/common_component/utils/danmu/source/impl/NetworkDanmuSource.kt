package com.xyoye.common_component.utils.danmu.source.impl

import com.xyoye.common_component.network.repository.ResourceRepository

import com.xyoye.common_component.utils.danmu.source.AbstractDanmuSource
import java.io.InputStream

/**
 * Created by xyoye on 2024/1/14.
 */

class NetworkDanmuSource(
    private val url: String,
    private val headers: Map<String, String> = emptyMap()
) : AbstractDanmuSource() {

    override suspend fun getStream(): InputStream? {
        return ResourceRepository.getResourceResponseBody(url, headers)
            .getOrNull()
            ?.byteStream()
    }
}