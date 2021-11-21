package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.inter.VideoSource
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/21.
 */

class StreamMediaSource(
    private val videoUrl: String,
    private val header: Map<String, String>?
) : VideoSource {
    override fun getVideoUrl(): String {
        return videoUrl
    }

    override fun getVideoTitle(): String {
        return getFileName(videoUrl)
    }

    override fun getCurrentPosition(): Long {
        return 0L
    }

    override fun getMediaType(): MediaType {
        return MediaType.STREAM_LINK
    }

    override fun getHttpHeader(): Map<String, String>? {
        return header
    }
}