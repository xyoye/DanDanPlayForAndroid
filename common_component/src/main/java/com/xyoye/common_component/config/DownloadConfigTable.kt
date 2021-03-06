package com.xyoye.common_component.config

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

/**
 * Created by xyoye on 2020/12/29.
 */

@MMKVKotlinClass(className = "DownloadConfig")
object DownloadConfigTable {

    //是否开启DHT网络
    @MMKVFiled
    val dhtEnable = true

    //最大同时下载任务数量
    @MMKVFiled
    val maxDownloadTask = 4
}