package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2022/1/2.
 */

data class LoadDanmuResult(
    val videoUrl: String,
    val danmuPath: String = "",
    val episodeId: Int = 0,
    val isHistoryData: Boolean = false
)