package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class AnimeData(
    var animeId: Int = 0,
    var animeTitle: String? = null,
    var imageUrl: String? = null,
    var searchKeyword: String? = null,
    var isOnAir: Boolean = false,
    var airDay: Int = 0,
    var isFavorited: Boolean = false,
    var isRestricted: Boolean = false,
    var rating: Double = 0.0,
    var startDate: String? = null
) : Parcelable