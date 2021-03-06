package com.xyoye.common_component.config

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

/**
 * Created by xyoye on 2021/1/6.
 */

@MMKVKotlinClass(className = "UserConfig")
object UserConfigTable {
    //用户是否已登录
    @MMKVFiled
    const val userLoggedIn = false

    //用户token
    @MMKVFiled
    const val userToken = ""

    //用户头像索引
    @MMKVFiled
    const val userCoverIndex = -1
}