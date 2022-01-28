package com.xyoye.data_component.bean


/**
 * Created by xyoye on 2022/1/25
 */
data class DanmuSourceHeaderBean(
    val animeId: Int,
    val animeName: String,
    val episodeData: List<DanmuSourceContentBean>,
    var isSelected: Boolean = false,
    var isRecommend: Boolean = false,
    var isLoaded: Boolean = false
)