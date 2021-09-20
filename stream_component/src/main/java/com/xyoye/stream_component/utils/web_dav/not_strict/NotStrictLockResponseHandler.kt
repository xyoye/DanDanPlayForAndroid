package com.xyoye.stream_component.utils.web_dav.not_strict

import com.thegrizzlylabs.sardineandroid.impl.handler.LockResponseHandler
import com.thegrizzlylabs.sardineandroid.model.Prop
import java.io.InputStream

/**
 * Created by xyoye on 2021/9/20.
 */

class NotStrictLockResponseHandler: LockResponseHandler() {
    override fun getToken(stream: InputStream?): String {
        val prop = NotStrictSardineUtil.unmarshal(Prop::class.java, stream, false)
        return prop.lockdiscovery.activelock.iterator().next().locktoken.href
    }
}