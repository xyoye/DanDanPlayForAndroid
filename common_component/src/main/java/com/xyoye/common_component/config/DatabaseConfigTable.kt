package com.xyoye.common_component.config

import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled


/**
 * Created by xyoye on 2022/1/24
 */

@MMKVClass(className = "DatabaseConfig")
object DatabaseConfigTable {

    @MMKVFiled
    const val isMigrated_6_7 = false
}