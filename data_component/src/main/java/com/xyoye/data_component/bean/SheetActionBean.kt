package com.xyoye.data_component.bean

import androidx.annotation.DrawableRes

/**
 * Created by xyoye on 2020/11/18.
 */

data class SheetActionBean(
    val actionId: Any,

    val actionName: String,

    @DrawableRes val actionIconRes: Int = -1,

    val describe: String? = null
)