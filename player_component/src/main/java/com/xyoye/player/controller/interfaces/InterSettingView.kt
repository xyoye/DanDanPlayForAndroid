package com.xyoye.player.controller.interfaces

import com.xyoye.data_component.enums.SettingViewType

/**
 * Created by xyoye on 2020/11/14.
 */

interface InterSettingView : InterControllerView{

    fun getSettingViewType(): SettingViewType

    fun onSettingVisibilityChanged(isVisible: Boolean)

    fun isSettingShowing(): Boolean
}