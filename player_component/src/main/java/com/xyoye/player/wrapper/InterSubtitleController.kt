package com.xyoye.player.wrapper

import com.xyoye.data_component.bean.VideoStreamBean
import com.xyoye.subtitle.MixedSubtitle

/**
 * Created by xyoye on 2021/4/15.
 */

interface InterSubtitleController {

    /**
     * 添加字幕流
     */
    fun addSubtitleStream(filePath: String)

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
     * 获取外挂字幕流
     */
    fun getExternalSubtitleStream(): List<VideoStreamBean>

    /**
     * 选择字幕流
     */
    fun selectSubtitleStream(stream: VideoStreamBean)

    /**
     * 字幕输出
     */
    fun onSubtitleTextOutput(subtitle: MixedSubtitle)
}