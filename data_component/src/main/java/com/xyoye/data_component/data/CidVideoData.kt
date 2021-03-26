package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/3/26.
 */

@JsonClass(generateAdapter = true)
data class CidVideoBean(
    val videoData: VideoDataBean
)

@JsonClass(generateAdapter = true)
data class VideoDataBean(
    val title: String,
    val cid: Long
)