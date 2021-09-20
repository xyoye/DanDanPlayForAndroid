package com.xyoye.stream_component.utils.web_dav

import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.xyoye.stream_component.utils.web_dav.not_strict.NotStrictOkHttpSardine
import okhttp3.OkHttpClient

/**
 * Created by xyoye on 2021/9/20.
 */

object DavClientFactory {

    fun getInstance(client: OkHttpClient, strict: Boolean = true): OkHttpSardine {
        return if (strict)
            OkHttpSardine(client)
        else
            NotStrictOkHttpSardine(client)
    }
}