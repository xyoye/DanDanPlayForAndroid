package com.xyoye.common_component.config

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

@MMKVKotlinClass(className = "AppConfig")
object AppConfigTable {
    //是否展示欢迎页
    @MMKVFiled
    const val showSplashAnimation = false

    //缓存路径
    @MMKVFiled
    val cachePath = DefaultConfig.DEFAULT_CACHE_PATH

    //是否展示隐藏文件
    @MMKVFiled
    var showHiddenFile = false

    @MMKVFiled
    //是否展示FTP播放视频提示
    var showFTPVideoTips = true

    @MMKVFiled
    //磁链搜索节点
    var magnetResDomain: String? = null

    @MMKVFiled
    //最后一次更新云屏蔽信息的时间
    var cloudBlockUpdateTime: Long = 0
}