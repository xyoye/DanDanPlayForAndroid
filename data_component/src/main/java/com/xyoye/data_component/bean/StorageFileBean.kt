package com.xyoye.data_component.bean

import java.util.*


/**
 * Created by xyoye on 2022/1/19
 */
data class StorageFileBean(
    val isDirectory: Boolean,
    val filePath: String,
    val fileName: String,
    val danmuPath: String? = null,
    val subtitlePath: String? = null,
    val position: Long = 0,
    val duration: Long = 0,
    val uniqueKey: String? = null,
    val childFileCount: Int = 0,
    val fileCoverUrl: String? = null,
    val lastPlayTime: Date? = null,
    var isLastPlay: Boolean = false
)