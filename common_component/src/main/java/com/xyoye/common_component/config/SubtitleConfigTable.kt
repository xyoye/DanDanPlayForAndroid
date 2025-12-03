package com.xyoye.common_component.config

import android.graphics.Color
import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled

/**
 * Created by xyoye on 2020/9/21.
 */

@MMKVClass(className = "SubtitleConfig")
object SubtitleConfigTable {

    @MMKVFiled
    const val shooterSecret = ""

    //字幕文字大小百分比
    @MMKVFiled
    const val textSize = 50

    //字幕描边百分比
    @MMKVFiled
    const val strokeWidth = 50

    //字幕文字颜色所在位置百分比
    @MMKVFiled
    const val textColor = Color.WHITE

    //字幕描边颜色所在位置百分比
    @MMKVFiled
    const val strokeColor = Color.BLACK

    /**
     * -------------播放器设置-----------
     */
    //自动加载同名字幕
    @MMKVFiled
    const val autoLoadSameNameSubtitle = true

    //自动匹配同名字幕
    @MMKVFiled
    const val autoMatchSubtitle = true

    //字幕优先级
    @MMKVFiled
    val subtitlePriority: String? = null
}