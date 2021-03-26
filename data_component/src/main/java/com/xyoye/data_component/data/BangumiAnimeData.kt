package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class BangumiAnimeData(
    var bangumiList: MutableList<AnimeData> = mutableListOf()
) : CommonJsonData(), Parcelable