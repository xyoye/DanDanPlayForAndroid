package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2022/1/2.
 */

data class LoadDanmuResult constructor(
    val videoUrl: String,
    val danmu: LocalDanmuBean?,
    val isHistoryData: Boolean = false
)