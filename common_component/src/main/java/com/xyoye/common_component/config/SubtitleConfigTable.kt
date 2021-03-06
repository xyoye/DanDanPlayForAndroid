package com.xyoye.common_component.config

import android.graphics.Color
import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

/**
 * Created by xyoye on 2020/9/21.
 */

@MMKVKotlinClass(className = "SubtitleConfig")
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

    //自动加载本地字幕
    @MMKVFiled
    const val autoLoadLocalSubtitle = true

    //自动加载网络字幕
    @MMKVFiled
    const val autoLoadNetworkSubtitle = false

    //网络视频自动加载同名字幕
    @MMKVFiled
    const val autoLoadSubtitleNetworkStorage = true
}