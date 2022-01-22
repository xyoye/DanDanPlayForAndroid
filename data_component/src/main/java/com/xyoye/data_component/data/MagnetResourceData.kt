package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/8/19.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class MagnetResourceData(
    var HasMore: Boolean = false,
    var Resources: MutableList<MagnetData>? = null
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class MagnetData(
    var Title: String? = null,
    var TypeId: Int = -1,
    var TypeName: String? = null,
    var SubgroupId: Int = -1,
    var SubgroupName: String? = null,
    var Magnet: String? = null,
    var PageUrl: String? = null,
    var FileSize: String? = null,
    var PublishDate: String? = null,
    var episodeId: Int = -1
) : Parcelable