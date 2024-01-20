package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2022/1/2.
 */

data class LoadDanmuResult constructor(
    val videoUrl: String,
    val danmuPath: String = "",
    val episodeId: String? = null,
    val isHistoryData: Boolean = false
)