package com.xyoye.common_component.config

import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/1/22
 *    desc  : 开发者配置表
 */

@MMKVClass(className = "DevelopConfig")
object DevelopConfigTable {

    // AppId
    @MMKVFiled
    const val appId = ""

    // App Secret
    @MMKVFiled
    const val appSecret = ""

    // 是否已自动显示认证弹窗
    @MMKVFiled
    const val isAutoShowAuthDialog = false
}