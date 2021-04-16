package com.xyoye.player.controller.video

/**
 * Created by xyoye on 2020/11/2.
 */

interface InterGestureView : InterControllerView {

    fun onStartSlide()

    fun onStopSlide()

    fun onPositionChange(newPosition: Long, currentPosition: Long, duration: Long)

    fun onBrightnessChange(percent: Int)

    fun onVolumeChange(percent: Int)
}