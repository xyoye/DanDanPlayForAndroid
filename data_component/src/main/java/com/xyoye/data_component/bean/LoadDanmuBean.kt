package com.xyoye.data_component.bean

import com.xyoye.data_component.enums.LoadDanmuState

/**
 * Created by xyoye on 2022/1/2.
 */

data class LoadDanmuBean(
    val videoUrl: String,
    var state: LoadDanmuState = LoadDanmuState.NOT_SUPPORTED,
    var danmuPath: String? = null,
    var episodeId: Int = 0
)