package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/2/21.
 */

@JsonClass(generateAdapter = true)
data class SendDanmuData(
    val cid: String?
)