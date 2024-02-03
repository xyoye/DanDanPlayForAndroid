package com.xyoye.data_component.data.alist

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.xyoye.data_component.helper.moshi.NullToEmptyString
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2024/1/20.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class AlistRootData(
    @NullToEmptyString
    @Json(name = "base_path")
    val rootPath: String = ""
) : Parcelable