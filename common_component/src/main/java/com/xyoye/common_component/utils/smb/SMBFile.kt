package com.xyoye.common_component.utils.smb

/**
 * Created by xyoye on 2021/2/3.
 */

data class SMBFile(
    val name: String,
    val size: Long,
    val isDirectory: Boolean
)