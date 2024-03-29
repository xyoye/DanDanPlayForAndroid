package com.xyoye.player.wrapper

import android.view.KeyEvent
import com.xyoye.data_component.enums.SettingViewType

/**
 * Created by xyoye on 2021/4/14.
 */

interface InterSettingController {

    /**
     * 当前视图是否正在显示
     */
    fun isSettingViewShowing(): Boolean

    /**
     * 根据 viewType 显示对应的视图
     */
    fun showSettingView(viewType: SettingViewType, extra: Any? = null)

    /**
     * 隐藏所有面板设置类视图
     */
    fun hideSettingView()

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean

    fun settingRelease()
}