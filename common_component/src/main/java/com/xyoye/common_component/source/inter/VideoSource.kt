package com.xyoye.common_component.source.inter

import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/14.
 *
 * 视频资源
 */

interface VideoSource {
    fun getVideoUrl(): String

    fun getVideoTitle(): String

    fun getCurrentPosition(): Long

    fun indexTitle(index: Int): String

    fun getMediaType(): MediaType

    fun getHttpHeader(): Map<String, String>?
}