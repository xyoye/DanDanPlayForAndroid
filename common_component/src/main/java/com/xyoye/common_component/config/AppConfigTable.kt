package com.xyoye.common_component.config

import androidx.appcompat.app.AppCompatDelegate
import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled
import com.xyoye.common_component.network.config.Api
import com.xyoye.common_component.utils.meida.VideoExtension
import com.xyoye.data_component.enums.HistorySort
import com.xyoye.data_component.enums.StorageSort

@MMKVClass(className = "AppConfig")
object AppConfigTable {
    //是否展示欢迎页
    @MMKVFiled
    const val showSplashAnimation = false

    //缓存路径
    @MMKVFiled
    val cachePath = DefaultConfig.DEFAULT_CACHE_PATH

    //是否展示隐藏文件
    @MMKVFiled
    val showHiddenFile = false

    @MMKVFiled
    //是否展示FTP播放视频提示
    val showFTPVideoTips = true

    @MMKVFiled
    //磁链搜索节点
    val magnetResDomain: String? = null

    @MMKVFiled
    //最后一次更新云屏蔽信息的时间
    val cloudBlockUpdateTime: Long = 0

    @MMKVFiled
    //深色模式状态
    val darkMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    @MMKVFiled
    //常用目录1
    val commonlyFolder1: String? = null

    @MMKVFiled
    //常用目录2
    val commonlyFolder2: String? = null

    @MMKVFiled
    //上次打开目录
    val lastOpenFolder: String? = null

    @MMKVFiled
    //上次打开目录开关
    val lastOpenFolderEnable: Boolean = true

    @MMKVFiled
    //上次搜索弹幕记录
    val lastSearchDanmuJson: String? = null

    @MMKVFiled
    //文件排序类型
    val storageSortType: Int = StorageSort.NAME.value

    @MMKVFiled
    //文件排序升序
    val storageSortAsc: Boolean = true

    @MMKVFiled
    //文件排序文件夹优先
    val storageSortDirectoryFirst: Boolean = true

    @MMKVFiled
    //播放历史排序类型
    val historySortType: Int = HistorySort.TIME.value

    @MMKVFiled
    //播放历史排序升序
    val historySortAsc: Boolean = false

    @MMKVFiled
    //是否启用备用域名
    val backupDomainEnable: Boolean = false

    @MMKVFiled
    //备用域名地址
    val backupDomain: String = Api.DAN_DAN_SPARE

    @MMKVFiled
    //支持的视频后缀
    val supportVideoExtension: String? = VideoExtension.supportText

    @MMKVFiled
    // Jsoup的User-Agent
    val jsoupUserAgent: String = DefaultConfig.DEFAULT_JSOUP_USER_AGENT
}