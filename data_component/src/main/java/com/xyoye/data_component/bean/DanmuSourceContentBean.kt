package com.xyoye.data_component.bean


/**
 * Created by xyoye on 2022/1/25
 */
data class DanmuSourceContentBean(
    val animeTitle: String,
    val episodeTitle: String,
    val episodeId: Int,
    val isRecommend: Boolean = false,
    var isLoaded: Boolean = false
)