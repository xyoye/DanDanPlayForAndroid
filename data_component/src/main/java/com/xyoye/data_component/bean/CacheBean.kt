package com.xyoye.data_component.bean

import com.xyoye.data_component.enums.CacheType

/**
 * Created by xyoye on 2022/1/16.
 */

data class CacheBean(
    val cacheType: CacheType?,
    val fileCount: Int,
    val totalSize: Long
)