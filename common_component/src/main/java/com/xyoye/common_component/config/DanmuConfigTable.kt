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

    //滚动弹幕最大显示行数
    @MMKVFiled
    const val danmuScrollMaxLine = -1

    //顶部弹幕最大显示行数
    @MMKVFiled
    const val danmuTopMaxLine = -1

    //底部弹幕最大显示行数
    @MMKVFiled
    const val danmuBottomMaxLine = -1

    //开启弹幕云屏蔽
    @MMKVFiled
    const val cloudDanmuBlock = true

    /**
     * -------------播放器设置-----------
     */
    //自动加载同名弹幕
    @MMKVFiled
    const val autoLoadSameNameDanmu = true

    //自动匹配同名弹幕
    @MMKVFiled
    const val autoMatchDanmu = true

    //根据屏幕刷新率绘制弹幕
    @MMKVFiled
    const val danmuUpdateInChoreographer = true

    @MMKVFiled
    const val danmuDebug = false

    /**
     * -------------弹幕下载-----------
     */
    @MMKVFiled
    const val showThirdSource = false

    @MMKVFiled
    const val defaultLanguage = 0
}