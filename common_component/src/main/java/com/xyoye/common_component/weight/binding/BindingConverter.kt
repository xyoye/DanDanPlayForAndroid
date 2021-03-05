package com.xyoye.common_component.weight.binding

import androidx.databinding.InverseMethod

/**
 * Created by xyoye on 2021/1/26.
 */

object BindingConverter {

    @InverseMethod("stringToInt")
    @JvmStatic
    fun intToString(oldValue: Int): String {
        return oldValue.toString()
    }

    @JvmStatic
    fun stringToInt(oldValue: String): Int {
        if (oldValue.isEmpty())
            return 0
        return oldValue.toInt()
    }
}