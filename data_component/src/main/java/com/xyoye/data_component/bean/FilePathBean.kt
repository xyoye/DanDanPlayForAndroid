package com.xyoye.data_component.bean

/**
 * 路径信息
 */
data class FilePathBean(
    var name: String,
    val path: String,
    var isOpened: Boolean = false
)