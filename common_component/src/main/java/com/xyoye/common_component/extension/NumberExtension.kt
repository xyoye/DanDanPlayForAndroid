package com.xyoye.common_component.extension

import android.graphics.Color

/**
 * Created by xyoye on 2024/2/1
 */

/**
 * 限制最大最小范围
 */
fun Int.clamp(min: Int, max: Int): Int {
    return this.coerceAtLeast(min).coerceAtMost(max)
}

fun Float.clamp(min: Float, max: Float): Float {
    return this.coerceAtLeast(min).coerceAtMost(max)
}

/**
 * 颜色透明度
 */
fun Int.opacity(percent: Float): Int {
    return Color.argb(
        (percent.clamp(0f, 1f) * 255).toInt(),
        Color.red(this),
        Color.green(this),
        Color.blue(this)
    )
}