package com.xyoye.common_component.utils

/**
 * Created by xyoye on 2021/3/28.
 */

class RemoteHelper private constructor() {

    var remoteUrl: String? = null
    var remoteToken: String? = null

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = RemoteHelper()
    }

    fun buildImageUrl(hash: String): String {
        var imageUrl = remoteUrl + "api/v1/image/$hash"
        if (!remoteToken.isNullOrEmpty()) {
            imageUrl += "&token=$remoteToken"
        }
        return imageUrl
    }

    fun buildVideoUrl(hash: String): String {
        var videoUrl = remoteUrl + "api/v1/stream/$hash"
        if (!remoteToken.isNullOrEmpty()) {
            videoUrl += "&token=$remoteToken"
        }
        return videoUrl
    }
}