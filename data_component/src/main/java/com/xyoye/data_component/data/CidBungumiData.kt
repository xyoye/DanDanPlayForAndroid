package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/3/26.
 */

@JsonClass(generateAdapter = true)
data class CidBungumiData(
    val mediaInfo: BungumiMediaInfo,
    val epList: MutableList<MediaInfoCid> = mutableListOf()
)

@JsonClass(generateAdapter = true)
data class BungumiMediaInfo(
    val title: String
)

@JsonClass(generateAdapter = true)
data class MediaInfoCid(
    val cid: Long
)