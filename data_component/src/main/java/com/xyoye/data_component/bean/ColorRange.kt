package com.xyoye.data_component.bean

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

/**
 *    author: xieyy@anjiu-tech.com
 *    time  : 2023/8/31
 *    desc  : 存储两组颜色，可根据百分比取出中间颜色
 */

class ColorRange(
    @ColorInt val start: Int,
    @ColorInt val end: Int,
) {
    fun take(@FloatRange(from = 0.0, to = 1.0) percent: Float): Int {
        val red = (Color.red(start) + (Color.red(end) - Color.red(start)) * percent).toInt()
        val green = (Color.green(start) + (Color.green(end) - Color.green(start)) * percent).toInt()
        val blue = (Color.blue(start) + (Color.blue(end) - Color.blue(start)) * percent).toInt()
        val alpha = (Color.alpha(start) + (Color.alpha(end) - Color.alpha(start)) * percent).toInt()
        return Color.argb(alpha, red, green, blue)
    }
}