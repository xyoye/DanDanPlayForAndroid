package com.xyoye.common_component.utils

import android.os.SystemClock

/**
 * Created by xyoye on 2020/10/13.
 */

class FastClickFilter {
    companion object {
        var lastClickTimeMS = 0L

        fun isNeedFilter(): Boolean {
            val currentTimeMS = SystemClock.uptimeMillis()
            if (currentTimeMS - lastClickTimeMS > 1000) {
                lastClickTimeMS = currentTimeMS
                return false
            }
            return true
        }
    }
}