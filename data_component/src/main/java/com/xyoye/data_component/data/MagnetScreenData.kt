package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import com.xyoye.data_component.helper.moshi.NullToEmptyString
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/10/26.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class MagnetScreenData(
    val Id: Int = -1,
    @NullToEmptyString
    val Name: String = ""
) : Parcelable