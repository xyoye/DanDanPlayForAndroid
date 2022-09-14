package com.xyoye.common_component.config

import androidx.appcompat.app.AppCompatDelegate
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

    @MMKVFiled
    //深色模式状态
    var darkMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    @MMKVFiled
    //常用目录1
    var commonlyFolder1: String? = null

    @MMKVFiled
    //常用目录2
    var commonlyFolder2: String? = null

    @MMKVFiled
    //上次打开目录
    var lastOpenFolder: String? = null

    @MMKVFiled
    //上次打开目录开关
    var lastOpenFolderEnable: Boolean = true

    @MMKVFiled
    //上次搜索弹幕记录
    var lastSearchDanmuJson: String? = null

    @MMKVFiled
    //是否使用投屏接收密码
    var useScreencastPwd: Boolean = false

    @MMKVFiled
    //上次使用的投屏接收密码
    var lastUsedScreencastPwd: String? = null
}