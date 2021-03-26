package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
@JsonClass(generateAdapter = true)
open class CommonJsonData(
    var errorCode: Int = 0,
    var success: Boolean = false,
    var errorMessage: String? = null
) : Parcelable