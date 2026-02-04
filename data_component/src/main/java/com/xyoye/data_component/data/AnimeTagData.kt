package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2021/2/21.
 */

@Parcelize
@Serializable
data class AnimeTagData(
    val animes: List<AnimeData> = emptyList()
): Parcelable