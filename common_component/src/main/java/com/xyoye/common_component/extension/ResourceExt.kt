package com.xyoye.common_component.extension

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.xyoye.common_component.base.app.BaseApplication

/**
 * Created by xyoye on 2021/3/20.
 */

fun Int.toResColor(): Int{
    return ContextCompat.getColor(BaseApplication.getAppContext(), this)
}

fun Int.toResDrawable(): Drawable?{
    return ContextCompat.getDrawable(BaseApplication.getAppContext(), this)
}

fun Int.toResString(): String{
    return BaseApplication.getAppContext().resources.getString(this)
}