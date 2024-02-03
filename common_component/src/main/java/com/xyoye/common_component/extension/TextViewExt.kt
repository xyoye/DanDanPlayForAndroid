package com.xyoye.common_component.extension

import android.widget.TextView
import androidx.annotation.ColorRes

/**
 * Created by xyoye on 2020/12/24.
 */

fun TextView.setTextColorRes(@ColorRes colorRes: Int) {
    setTextColor(colorRes.toResColor(context))
}