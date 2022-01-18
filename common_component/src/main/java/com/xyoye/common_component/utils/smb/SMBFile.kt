package com.xyoye.common_component.utils.smb

/**
 * Created by xyoye on 2021/2/3.
 */

data class SMBFile(
    val name: String,
    val size: Long,
    val isDirectory: Boolean,
    val danmuPath: String? = null,
    val subtitlePath: String? = null,
    val position: Long = 0,
    val duration: Long = 0,
    val uniqueKey: String? = null
)