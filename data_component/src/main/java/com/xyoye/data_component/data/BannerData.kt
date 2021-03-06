package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
data class BannerData(
    var banners: MutableList<BannerDetailData> = mutableListOf()
) : CommonJsonData()

@Parcelize
data class BannerDetailData(
    var title: String? = null,
    var description: String? = null,
    var url: String? = null,
    var imageUrl: String? = null
) : Parcelable