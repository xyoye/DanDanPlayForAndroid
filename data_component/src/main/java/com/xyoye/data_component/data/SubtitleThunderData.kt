package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2020/11/30.
 */

data class SubtitleThunderData(
    val sublist: MutableList<ThunderData>?
)

/**
 * scid : 7D3B4991BD812C3F4C30485570DCACEDDF6BE184
 * sname : [DHR&Hakugetsu][Sword Art Online][01][BDRip][1080P][AVC_Hi10P_FLAC][FBDAC466].tc.ass
 * language : 繁体
 * rate : 3
 * surl : http://subtitle.v.geilijiasu.com/7D/3B/7D3B4991BD812C3F4C30485570DCACEDDF6BE184.ass
 * svote : 3457
 * roffset : 5426795912
 */
@JsonClass(generateAdapter = true)
data class ThunderData(
    val scid: String?,
    val sname: String?,
    val language: String?,
    val rate: String?,
    val surl: String?,
    val svote: Int,
    val roffset: Long
)