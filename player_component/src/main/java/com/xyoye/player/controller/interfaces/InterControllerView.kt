package com.xyoye.player.controller.interfaces

import android.graphics.Point
import android.view.View
import com.xyoye.player.controller.wrapper.ControlWrapper
import com.xyoye.data_component.enums.PlayState

/**
 * Created by xyoye on 2020/11/1.
 */

interface InterControllerView {

    fun attach(controlWrapper: ControlWrapper)

    fun getView(): View

    fun onVisibilityChanged(isVisible: Boolean)

    fun onPlayStateChanged(playState: PlayState)

    fun onProgressChanged(duration: Long, position: Long)

    fun onLockStateChanged(isLocked: Boolean)

    fun onVideoSizeChanged(videoSize: Point)
}