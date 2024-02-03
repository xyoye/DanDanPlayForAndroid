package com.xyoye.player.controller.video

import android.graphics.Point
import android.view.View
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.TrackType
import com.xyoye.player.wrapper.ControlWrapper

/**
 * Created by xyoye on 2020/11/1.
 */

interface InterControllerView {

    fun attach(controlWrapper: ControlWrapper)

    fun getView(): View

    fun onVisibilityChanged(isVisible: Boolean) {}

    fun onPlayStateChanged(playState: PlayState) {}

    fun onProgressChanged(duration: Long, position: Long) {}

    fun onLockStateChanged(isLocked: Boolean) {}

    fun onVideoSizeChanged(videoSize: Point) {}

    fun onPopupModeChanged(isPopup: Boolean) {}

    fun onTrackChanged(type: TrackType) {}
}