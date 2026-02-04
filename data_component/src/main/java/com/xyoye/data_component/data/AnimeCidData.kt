package com.xyoye.data_component.data

/**
 * Created by xyoye on 2021/3/26.
 */

data class AnimeCidData(
    val animeTitle: String = "",
    val episodes: List<EpisodeCidData> = emptyList()
)