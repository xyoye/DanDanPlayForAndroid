package com.xyoye.stream_component.utils.web_dav.not_strict

import com.thegrizzlylabs.sardineandroid.impl.handler.MultiStatusResponseHandler
import com.thegrizzlylabs.sardineandroid.model.Multistatus
import java.io.InputStream

/**
 * Created by xyoye on 2021/9/20.
 */

class NotStrictMultiStatusResponseHandler : MultiStatusResponseHandler() {
    override fun getMultistatus(stream: InputStream?): Multistatus {
        return NotStrictSardineUtil.unmarshal(Multistatus::class.java, stream, false)
    }
}