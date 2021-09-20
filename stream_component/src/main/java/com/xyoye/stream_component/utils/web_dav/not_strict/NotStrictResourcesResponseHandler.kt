package com.xyoye.stream_component.utils.web_dav.not_strict

import android.util.Log
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.handler.ResourcesResponseHandler
import okhttp3.Response
import java.net.URISyntaxException
import java.util.*

/**
 * Created by xyoye on 2021/9/20.
 */

class NotStrictResourcesResponseHandler: ResourcesResponseHandler() {

    companion object {
        private val TAG = NotStrictResourcesResponseHandler::class.java.simpleName
    }

    override fun handleResponse(response: Response?): MutableList<DavResource> {
        val multiStatus = NotStrictMultiStatusResponseHandler().handleResponse(response)
        val davResponses = multiStatus.response
        val resources: MutableList<DavResource> = ArrayList(davResponses.size)
        for (davResponse in davResponses) {
            try {
                resources.add(DavResource(davResponse))
            } catch (e: URISyntaxException) {
                Log.w(
                    TAG,
                    String.format("Ignore resource with invalid URI %s", davResponse.href)
                )
            }
        }
        return resources
    }
}