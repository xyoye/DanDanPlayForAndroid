package com.xyoye.player.wrapper

import com.xyoye.subtitle.MixedSubtitle

/**
 * Created by xyoye on 2021/4/15.
 */

interface InterSubtitleController : InterVideoTrack {

    /**
     * 更新字幕偏移时间
     */
    fun updateSubtitleOffsetTime()

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
     * 字幕输出
     */
    fun onSubtitleTextOutput(subtitle: MixedSubtitle)
}