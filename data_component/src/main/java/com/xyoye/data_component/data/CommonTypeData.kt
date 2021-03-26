package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2020/10/12.
 */

@JsonClass(generateAdapter = true)
data class CommonTypeData(
    var typeName: String,
    var typeId: String,

    var isChecked: Boolean = false,
    var isEnable: Boolean = true
)