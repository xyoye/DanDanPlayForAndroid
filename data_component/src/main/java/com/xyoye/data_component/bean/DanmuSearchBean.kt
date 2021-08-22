package com.xyoye.data_component.bean

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/8/22.
 */

@JsonClass(generateAdapter = true)
data class DanmuSearchBean(
    val episodeId: String = "",
    val animeName: String = ""
)