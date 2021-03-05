package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/8/19.
 */

@Parcelize
data class MagnetResourceData(
    var HasMore: Boolean = false,
    var Resources: MutableList<MagnetData>? = null
) : Parcelable

@Parcelize
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
    var episodeId: Int = -1,

    var position: Long = 0,
    var duration: Long = 0
) : Parcelable