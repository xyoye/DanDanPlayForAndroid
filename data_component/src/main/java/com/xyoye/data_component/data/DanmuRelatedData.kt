package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/25.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuRelatedData(
    var relateds: MutableList<DanmuRelatedUrlData> = mutableListOf()
) : CommonJsonData()

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuRelatedUrlData(
    val url: String?
) : Parcelable

