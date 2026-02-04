package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
@Serializable
data class BannerData(
    val banners: List<BannerDetailData> = emptyList()
) : CommonJsonData()

@Parcelize
@Serializable
data class BannerDetailData(
    var title: String? = null,
    var description: String? = null,
    var url: String? = null,
    var imageUrl: String? = null
) : Parcelable