package com.xyoye.cache

import com.danikula.videocache.source.Source
import com.danikula.videocache.source.SourceFactory


/**
 * Created by xyoye on 2021/12/31
 */
class OkHttpUrlSourceFactory: SourceFactory() {
    override fun createSource(url: String): Source {
        return OkHttpUrlSource(url, config)
    }
}