package com.xyoye.common_component.extension

import android.content.Context
import android.content.res.Configuration

/**
 * Created by xyoye on 2020/9/29.
 */

fun Context.isNightMode(): Boolean {
    val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mode == Configuration.UI_MODE_NIGHT_YES
}

fun Context.isLandScreen(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}