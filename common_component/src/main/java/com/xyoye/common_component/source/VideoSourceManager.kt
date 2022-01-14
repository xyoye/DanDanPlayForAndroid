package com.xyoye.common_component.source

import com.xyoye.common_component.source.base.BaseVideoSource

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

    private var videoSource: BaseVideoSource? = null

    fun setSource(source: BaseVideoSource) {
        videoSource = source
    }

    fun getSource() = videoSource
}