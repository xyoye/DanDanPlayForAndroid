package com.xyoye.player.controller.setting

import android.view.KeyEvent
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.controller.video.InterControllerView

/**
 * Created by xyoye on 2020/11/14.
 */

interface InterSettingView : InterControllerView {

    fun getSettingViewType(): SettingViewType

    fun onSettingVisibilityChanged(isVisible: Boolean)

    fun isSettingShowing(): Boolean

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
}