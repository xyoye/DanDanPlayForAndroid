package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2020/11/26.
 */

data class FileManagerBean(
    val filePath: String,
    val fileName: String,
    val isDirectory: Boolean,
    val isParent: Boolean = false
)