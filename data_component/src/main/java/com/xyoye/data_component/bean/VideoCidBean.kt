package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2021/3/26.
 */

data class VideoCidBean(
    val videoData: VideoDataBean
)

data class VideoDataBean(
    val title: String,
    val cid: Long
)