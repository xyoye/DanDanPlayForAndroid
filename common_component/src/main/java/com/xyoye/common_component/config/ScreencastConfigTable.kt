package com.xyoye.common_component.config

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

/**
 * Created by xyoye on 2022/9/15
 */

@MMKVKotlinClass(className = "ScreencastConfig")
object ScreencastConfigTable {

    @MMKVFiled
    //投屏接收端是否使用密码
    var useReceiverPassword: Boolean = false

    @MMKVFiled
    //投屏接收端密码
    var receiverPassword: String? = null

    @MMKVFiled
    //投屏接收端端口
    var receiverPort: Int = 0

    @MMKVFiled
    //接收到投屏时需手动确认
    var receiveNeedConfirm: Boolean = true

    @MMKVFiled
    //应用启动时，自动启动投屏接收服务
    var startReceiveWhenLaunch: Boolean = false
}