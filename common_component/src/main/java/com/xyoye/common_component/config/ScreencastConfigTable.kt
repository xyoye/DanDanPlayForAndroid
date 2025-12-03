package com.xyoye.common_component.config

import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled

/**
 * Created by xyoye on 2022/9/15
 */

@MMKVClass(className = "ScreencastConfig")
object ScreencastConfigTable {

    @MMKVFiled
    //投屏接收端是否使用密码
    val useReceiverPassword: Boolean = false

    @MMKVFiled
    //投屏接收端密码
    val receiverPassword: String? = null

    @MMKVFiled
    //投屏接收端端口
    val receiverPort: Int = 0

    @MMKVFiled
    //接收到投屏时需手动确认
    val receiveNeedConfirm: Boolean = true

    @MMKVFiled
    //应用启动时，自动启动投屏接收服务
    val startReceiveWhenLaunch: Boolean = false
}