package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
@Serializable
data class BangumiAnimeData(
    var bangumiList: List<AnimeData> = emptyList()
) : CommonJsonData(), Parcelable