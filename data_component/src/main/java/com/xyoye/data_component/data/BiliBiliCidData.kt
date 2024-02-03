package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/2/23.
 */

@JsonClass(generateAdapter = true)
data class BiliBiliCidData(
    val code: Int,
    val data: EpisodeCidData?
)