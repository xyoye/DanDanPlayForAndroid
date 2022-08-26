package com.xyoye.player.utils

import com.xyoye.player.wrapper.ControlWrapper

/**
 * Created by tiiime on 2022/7/27.
 * 长按倍速播放器
 */

class LongPressAccelerator(
    private val controlWrapper: ControlWrapper,
    private val onStart: (speed: Float) -> Unit,
    private val onStop: () -> Unit
) {
    private var originSpeed: Float = 1F
    private var isEnable = false

    fun enable() {
        //已暂停
        if (controlWrapper.isPlaying().not()) {
            return
        }
        //已锁屏
        if (controlWrapper.isLocked()) {
            return
        }
        //正在展示设置相关View
        if (controlWrapper.isSettingViewShowing()) {
            return
        }
        if (isEnable) {
            return
        }
        isEnable = true

        originSpeed = controlWrapper.getSpeed()
        val newSpeed = originSpeed * 2
        controlWrapper.setSpeed(newSpeed)
        onStart.invoke(newSpeed)
    }

    fun disable() {
        if (!isEnable) {
            return
        }
        isEnable = false

        controlWrapper.setSpeed(originSpeed)
        onStop.invoke()
    }
}