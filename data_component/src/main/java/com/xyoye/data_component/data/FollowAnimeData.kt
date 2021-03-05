package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/9.
 */

@Parcelize
data class FollowAnimeData(
    val favorites: MutableList<AnimeData> = mutableListOf()
) : CommonJsonData(), Parcelable