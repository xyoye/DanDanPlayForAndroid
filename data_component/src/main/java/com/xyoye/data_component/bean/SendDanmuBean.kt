package com.xyoye.data_component.bean

import android.graphics.Color

/**
 * Created by xyoye on 2021/2/21.
 */

data class SendDanmuBean(
    var position: Long,
    var text: String = "",
    var isSmallSize: Boolean = false,
    var isScroll : Boolean = true,
    var isTop: Boolean = false,
    var color: Int = Color.WHITE,
)