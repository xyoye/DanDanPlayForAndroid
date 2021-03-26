package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2021/3/26.
 */

data class BungumiCidBean(
    val mediaInfo: BungumiMediaInfo,
    val epList: MutableList<MediaInfoCid> = mutableListOf()
)

data class BungumiMediaInfo(
    val title: String
)

data class MediaInfoCid(
    val cid: Long
)