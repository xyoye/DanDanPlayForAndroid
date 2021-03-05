package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2020/8/17.
 */

data class AnimeDetailManagerBean(
    val animeDetailBeanList: MutableList<AnimeDetailBean>,
    val searchWord: String?,
    val animeTitle: String?
)