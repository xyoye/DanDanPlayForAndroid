package com.xyoye.player.controller.interfaces

import com.xyoye.data_component.enums.SettingViewType

/**
 * Created by xyoye on 2020/11/1.
 */

interface InterVideoController {

    fun startFadeOut()

    fun stopFadeOut()

    fun isControllerShowing(): Boolean

    fun isSettingViewShowing(): Boolean

    fun setLocked(locked: Boolean)

    fun isLocked(): Boolean

    fun startProgress()

    fun stopProgress()

    fun hideSettingView()

    fun hideController()

    fun showController()

    fun destroy()

    fun showSettingView(viewType: SettingViewType)

    fun seekTo(timeMs: Long)

    fun switchSubtitleSource()

    fun changeDanmuSource()
}