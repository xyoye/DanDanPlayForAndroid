package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/9.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class FollowAnimeData(
    val favorites: MutableList<AnimeData> = mutableListOf()
) : CommonJsonData(), Parcelable