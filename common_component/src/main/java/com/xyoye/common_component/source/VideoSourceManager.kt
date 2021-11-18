package com.xyoye.common_component.source

import com.xyoye.common_component.source.inter.VideoSource

/**
 * Created by xyoye on 2021/11/14.
 */

class VideoSourceManager private constructor() {

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = VideoSourceManager()
    }

    private var videoSource: VideoSource? = null

    fun setSource(source: VideoSource) {
        videoSource = source
    }

    fun getSource() = videoSource
}