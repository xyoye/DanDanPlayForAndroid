package com.xyoye.player.wrapper

import android.view.KeyEvent
import com.xyoye.data_component.enums.SettingViewType

/**
 * Created by xyoye on 2021/4/14.
 */

interface InterSettingController {

    /**
     * 切换弹幕、字幕资源
     */
    fun switchSource(isSwitchSubtitle: Boolean)

    /**
     * 当前视图是否正在显示
     */
    fun isSettingViewShowing(): Boolean

    /**
     * 根据 viewType 显示对应的视图
     */
    fun showSettingView(viewType: SettingViewType)

    /**
     * 隐藏所有面板设置类视图
     */
    fun hideSettingView()

    /**
     * 切换弹幕资源
     */
    fun onDanmuSourceChanged()

    /**
     * 切换字幕资源
     */
    fun onSubtitleSourceChanged()

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean

    fun settingRelease()
}