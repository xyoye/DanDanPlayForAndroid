package com.xyoye.common_component.config

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

/**
 * Created by xyoye on 2020/9/21.
 */

@MMKVKotlinClass(className = "DanmuConfig")
object DanmuConfigTable {

    //弹幕文字大小百分比
    @MMKVFiled
    const val danmuSize = 40

    //弹幕文字速度百分比
    @MMKVFiled
    const val danmuSpeed = 35

    //弹幕文字透明度百分比
    @MMKVFiled
    const val danmuAlpha = 100

    //弹幕描边百分比
    @MMKVFiled
    const val danmuStoke = 20

    //是否显示滚动弹幕
    @MMKVFiled
    const val showMobileDanmu = true

    //是否显示底部弹幕
    @MMKVFiled
    const val showBottomDanmu = true

    //是否显示顶部弹幕
    @MMKVFiled
    const val showTopDanmu = true

    //弹幕最大同屏数量
    @MMKVFiled
    const val danmuMaxCount = 0

    //弹幕最大显示行数
    @MMKVFiled
    const val danmuMaxLine = -1

    //开启弹幕云屏蔽
    @MMKVFiled
    const val cloudDanmuBlock = true

    /**
     * -------------播放器设置-----------
     */

    //自动加载本地弹幕
    @MMKVFiled
    const val autoLoadLocalDanmu = true

    //自动加载网络弹幕
    @MMKVFiled
    const val autoLoadNetworkDanmu = true

    //网络视频自动加载同名弹幕
    @MMKVFiled
    const val autoLoadDanmuNetworkStorage = true

    //网络视频自动匹配字幕
    @MMKVFiled
    const val autoMatchDanmuNetworkStorage = true

    //【外部视频】展示选择弹幕弹窗
    @MMKVFiled
    const val showDialogBeforePlay = true

    //【外部视频】自动进入选择弹幕
    @MMKVFiled
    const val autoLaunchDanmuBeforePlay = false

    @MMKVFiled
    const val danmuDebug = false
}