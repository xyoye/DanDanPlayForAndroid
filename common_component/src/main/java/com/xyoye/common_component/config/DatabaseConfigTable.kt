package com.xyoye.common_component.config

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass


/**
 * Created by xyoye on 2022/1/24
 */

@MMKVKotlinClass(className = "DatabaseConfig")
object DatabaseConfigTable {

    @MMKVFiled
    const val isMigrated_6_7 = false
}