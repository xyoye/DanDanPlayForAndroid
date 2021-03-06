package com.xyoye.data_component.data

/**
 * Created by xyoye on 2020/12/1.
 */

data class SubtitleSubData(
    val sub: SubData?
)

data class SubData(
    val subs: MutableList<SubDetailData>?
)

data class SubDetailData(
    val id: Int,
    val videoname: String?,
    val native_name: String?,
    val upload_time: String?,
    val subtype: String?,
    val lang: LanguageData?,

    val filename: String?,
    val size: Long,
    val url: String?,
    val filelist: MutableList<SubFileData>?
)

data class LanguageData(
    val desc: String?
)

data class SubFileData(
    val url: String?,
    //fileName
    val f: String?,
    //size
    val s: String?
)
