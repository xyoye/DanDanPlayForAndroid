package com.xyoye.common_component.extension

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.xyoye.common_component.R
import com.xyoye.common_component.base.app.BaseApplication

/**
 * Created by xyoye on 2021/3/20.
 */

fun Int.toResColor(context: Context = BaseApplication.getAppContext()): Int {
    return ContextCompat.getColor(context, this)
}

fun Int.toResDrawable(context: Context = BaseApplication.getAppContext()): Drawable? {
    return ContextCompat.getDrawable(context, this)
}

fun Int.toResString(context: Context = BaseApplication.getAppContext()): String {
    return context.resources.getString(this)
}

fun Int.colorWithAlpha(alpha: Int): Int {
    return Color.argb(
        alpha,
        Color.red(this),
        Color.green(this),
        Color.blue(this)
    )
}

fun rippleDrawable(@ColorRes rippleColorId: Int = R.color.gray_40): Drawable {
    return RippleDrawable(ColorStateList.valueOf(rippleColorId.toResColor()), null, null)
}