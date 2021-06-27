package com.xyoye.player.controller.base

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.xyoye.player.controller.video.InterGestureView

/**
 * Created by xyoye on 2021/5/30.
 */

abstract class TvVideoController(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseVideoController(context, attrs, defStyleAttr) {

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val intercept = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> onActionCenter()
            KeyEvent.KEYCODE_DPAD_UP -> onActionUp()
            KeyEvent.KEYCODE_DPAD_DOWN -> onActionDown()
            KeyEvent.KEYCODE_DPAD_LEFT -> onActionLeft()
            KeyEvent.KEYCODE_DPAD_RIGHT -> onActionRight()
            else -> false
        }
        return if (intercept) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private fun onActionCenter(): Boolean {
        if (isLocked()) {
            showController(true)
            return true
        }
        if (mControlWrapper.isSettingViewShowing()) {
            return false
        }
        if (isControllerShowing()) {
            showController(true)
            return false
        }
        togglePlay()
        return true
    }

    private fun onActionUp(): Boolean {
        if (mControlWrapper.isSettingViewShowing()) {
            return false
        }
        showController(true)
        return false
    }

    private fun onActionDown(): Boolean {
        if (mControlWrapper.isSettingViewShowing()) {
            return false
        }
        showController(true)
        return false
    }

    private fun onActionLeft(): Boolean {
        if (mControlWrapper.isSettingViewShowing()) {
            return false
        }
        if (isLocked() || isControllerShowing()) {
            showController(true)
            return false
        }
        changePosition(-10 * 1000L)
        return true
    }

    private fun onActionRight(): Boolean {
        if (mControlWrapper.isSettingViewShowing()) {
            return false
        }
        if (isLocked() || isControllerShowing()) {
            showController(true)
            return false
        }
        changePosition(10 * 1000L)
        return true
    }

    private fun changePosition(offset: Long) {
        val duration = mControlWrapper.getDuration()
        val currentPosition = mControlWrapper.getCurrentPosition()
        val newPosition = currentPosition + offset

        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onStartSlide()
                view.onPositionChange(newPosition, currentPosition, duration)
                view.onStopSlide()
            }
        }
        mControlWrapper.seekTo(newPosition)
    }
}