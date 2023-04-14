package com.xyoye.common_component.extension

import android.content.res.Resources

/**
 * Created by xyoye on 2023/1/8.
 */


fun Float.dp(): Float {
    val scale = Resources.getSystem().displayMetrics.density
    return this * scale + 0.5f
}