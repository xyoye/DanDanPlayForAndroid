package com.xyoye.data_component.bean

import com.xyoye.data_component.enums.VideoScreenScale

/**
 * Created by xyoye on 2020/11/16.
 */

data class VideoScaleBean(
    val screenScale: VideoScreenScale,

    val scaleName: String,

    var isChecked: Boolean = false
)