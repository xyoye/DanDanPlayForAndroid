package com.xyoye.data_component.data.alist

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2024/1/20.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class AlistDirectoryData(
    @Json(name = "total")
    val fileCount: Int = 0,

    @Json(name = "content")
    val fileList: List<AlistFileData> = emptyList()
) : Parcelable