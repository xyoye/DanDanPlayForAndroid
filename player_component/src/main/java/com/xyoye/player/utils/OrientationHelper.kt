package com.xyoye.player.utils

import android.content.Context
import android.os.SystemClock
import android.view.OrientationEventListener
import com.xyoye.player.controller.interfaces.OnOrientationChangeListener

/**
 * Created by xyoye on 2020/11/2.
 */

class OrientationHelper(context: Context) : OrientationEventListener(context) {

    private var mLastChangeTimeMs = 0L
    var mOnOrientationChangeListener: OnOrientationChangeListener? = null

    override fun onOrientationChanged(orientation: Int) {
        val currentTimeMs = SystemClock.uptimeMillis()
        if (currentTimeMs - mLastChangeTimeMs < 500) {
            return
        }

        mOnOrientationChangeListener?.onOrientationChanged(orientation)
        mLastChangeTimeMs = currentTimeMs
    }
}