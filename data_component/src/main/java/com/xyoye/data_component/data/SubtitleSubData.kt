package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2020/12/1.
 */

@JsonClass(generateAdapter = true)
data class SubtitleSubData(
    val sub: SubData?
)

@JsonClass(generateAdapter = true)
data class SubData(
    val subs: MutableList<SubDetailData>?
)

@JsonClass(generateAdapter = true)
data class SubDetailData(
    val id: Int,
    val videoname: String?,
    val native_name: String?,
    val upload_time: String?,
    val subtype: String?,
    val lang: LanguageData?,

    val filename: String?,
    val size: Long?,
    val url: String?,
    val filelist: MutableList<SubFileData>?
)

@JsonClass(generateAdapter = true)
data class LanguageData(
    val desc: String?
)

@JsonClass(generateAdapter = true)
data class SubFileData(
    val url: String?,
    //fileName
    val f: String?,
    //size
    val s: String?
)
