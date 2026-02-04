package com.xyoye.data_component.data

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/10/12.
 */

@Serializable
data class CommonTypeData(
    var typeName: String = "",
    var typeId: String = "",

    var isChecked: Boolean = false,
    var isEnable: Boolean = true
)