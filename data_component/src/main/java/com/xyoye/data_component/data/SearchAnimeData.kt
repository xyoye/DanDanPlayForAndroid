package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/10/13.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class SearchAnimeData(
    var animes: MutableList<AnimeData> = mutableListOf()
) : CommonJsonData(), Parcelable