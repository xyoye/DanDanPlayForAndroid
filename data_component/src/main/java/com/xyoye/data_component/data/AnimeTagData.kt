package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/2/21.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class AnimeTagData(
    val animes: MutableList<AnimeData> = mutableListOf()
): Parcelable