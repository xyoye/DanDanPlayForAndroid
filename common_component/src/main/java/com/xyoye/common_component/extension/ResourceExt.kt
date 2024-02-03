package com.xyoye.common_component.extension

import android.content.Context
import androidx.core.content.ContextCompat
import com.xyoye.common_component.utils.theme.themeContext

/**
 * Created by xyoye on 2021/3/20.
 */

fun Int.toResColor(context: Context = themeContext) =
    ContextCompat.getColor(context, this)

fun Int.toResDrawable(context: Context = themeContext) =
    ContextCompat.getDrawable(context, this)

fun Int.toResString(context: Context = themeContext) =
    context.resources.getString(this)

fun Int.dp(context: Context = themeContext) =
    (context.resources.displayMetrics.density * this + 0.5f).toInt()

fun Float.dp(context: Context = themeContext) =
    context.resources.displayMetrics.density * this + 0.5f