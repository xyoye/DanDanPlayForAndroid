package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/11/25.
 */

@Parcelize
@Serializable
data class DanmuRelatedData(
    var relateds: List<DanmuRelatedUrlData> = emptyList()
) : CommonJsonData()

@Parcelize
@Serializable
data class DanmuRelatedUrlData(
    val url: String = ""
) : Parcelable

