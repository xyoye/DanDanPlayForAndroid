package com.xyoye.common_component.utils

import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.WindowManager

/**
 * Created by xyoye on 2020/11/2.
 */

/**
 * 是否处于屏幕边缘
 */
fun Context.isScreenEdge(e: MotionEvent?): Boolean {
    if (e == null)
        return false
    val edgeSize = dp2px(40f)
    return e.rawX < edgeSize
            || e.rawX > getScreenWidth() - edgeSize
            || e.rawY < edgeSize
            || e.rawY > getScreenHeight() - edgeSize
}

/**
 * 获取屏幕宽度
 */
fun Context.getScreenWidth(isIncludeNav: Boolean = true): Int {
    return if (isIncludeNav) {
        resources.displayMetrics.widthPixels + getNavigationBarHeight()
    } else {
        resources.displayMetrics.widthPixels
    }
}

/**
 * 获取屏幕高度
 */
fun Context.getScreenHeight(isIncludeNav: Boolean = true): Int {
    return if (isIncludeNav) {
        resources.displayMetrics.heightPixels + getNavigationBarHeight()
    } else {
        resources.displayMetrics.heightPixels
    }
}

/**
 * 获取NavigationBar的高度
 */
private fun Context.getNavigationBarHeight(): Int {
    if (!hasNavigationBar()) {
        return 0
    }
    val resourceId = resources.getIdentifier(
        "navigation_bar_height",
        "dimen", "android"
    )
    //获取NavigationBar的高度
    return resources.getDimensionPixelSize(resourceId)
}

/**
 * 是否存在NavigationBar
 */
private fun Context.hasNavigationBar(): Boolean {
    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    val size = Point()
    val realSize = Point()
    display.getSize(size)
    display.getRealSize(realSize)
    return realSize.x != size.x || realSize.y != size.y
}