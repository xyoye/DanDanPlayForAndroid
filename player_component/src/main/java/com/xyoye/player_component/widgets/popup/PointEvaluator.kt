package com.xyoye.player_component.widgets.popup

import android.animation.TypeEvaluator
import android.graphics.Point


/**
 * Created by xyoye on 2023/3/25.
 */

class PointEvaluator : TypeEvaluator<Point> {
    override fun evaluate(fraction: Float, startValue: Point, endValue: Point): Point {
        val x = startValue.x + fraction * (endValue.x - startValue.x)
        val y = startValue.y + fraction * (endValue.y - startValue.y)
        return Point(x.toInt(), y.toInt())
    }
}