package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2020/11/26.
 */

data class DanmuSourceBean(
    val sourceName: String,
    val sourceUrl: String,
    val sourceDescribe: String,
    val isOfficial: Boolean = false,
    var isChecked: Boolean = false,
    var format: Int = 0
)