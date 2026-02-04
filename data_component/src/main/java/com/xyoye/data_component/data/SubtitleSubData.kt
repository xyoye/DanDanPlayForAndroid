package com.xyoye.data_component.data

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/12/1.
 */

@Serializable
data class SubtitleSubData(
    val sub: SubData? = null
)

@Serializable
data class SubData(
    val subs: List<SubDetailData> = emptyList()
)

@Serializable
data class SubDetailData(
    val id: Int = 0,
    val videoname: String = "",
    val native_name: String = "",
    val upload_time: String = "",
    val subtype: String = "",
    val lang: LanguageData? = null,

    val filename: String = "",
    val size: Long = 0,
    val url: String = "",
    val filelist: List<SubFileData> = emptyList()
)

@Serializable
data class LanguageData(
    val desc: String = ""
)

@Serializable
data class SubFileData(
    val url: String = "",
    //fileName
    val f: String = "",
    //size
    val s: String = ""
)
