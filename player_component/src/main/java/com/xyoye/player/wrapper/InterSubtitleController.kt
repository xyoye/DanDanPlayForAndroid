package com.xyoye.player.wrapper

/**
 * Created by xyoye on 2021/4/15.
 */

interface InterSubtitleController {

    /**
     * 设置字幕加载完成的回调
     */
    fun setDanmuLoadedCallback(callback: ((String, Boolean) -> Unit)?)

    /**
     * 设置图片字幕开关
     */
    fun setImageSubtitleEnable(enable: Boolean)

    /**
     * 设置文字字幕开关
     */
    fun setTextSubtitleDisable()

    /**
     * 是否显示外挂字幕
     */
    fun showExternalTextSubtitle()

    /**
     * 是否显示内置字幕
     */
    fun showInnerTextSubtitle()

    /**
     * 设置字幕路径
     */
    fun setSubtitlePath(subtitlePath: String, playWhenReady: Boolean = false)

    /**
     * 更新字幕文字大小
     */
    fun updateTextSize()

    /**
     * 更新字幕文字描边宽度
     */
    fun updateStrokeWidth()

    /**
     * 更新字幕文字颜色
     */
    fun updateTextColor()

    /**
     * 更新字幕文字描边颜色
     */
    fun updateStrokeColor()

    /**
     * 更新字幕偏移时间
     */
    fun updateOffsetTime()

    fun subtitleRelease()
}