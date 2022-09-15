package com.xyoye.common_component.config

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/15
 *     desc  :
 * </pre>
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
}