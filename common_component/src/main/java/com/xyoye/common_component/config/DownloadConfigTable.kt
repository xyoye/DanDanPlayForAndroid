package com.xyoye.common_component.config

import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled

/**
 * Created by xyoye on 2020/12/29.
 */

@MMKVClass(className = "DownloadConfig")
object DownloadConfigTable {

    //是否开启DHT网络
    @MMKVFiled
    val dhtEnable = true

    //最大同时下载任务数量
    @MMKVFiled
    val maxDownloadTask = 4
}