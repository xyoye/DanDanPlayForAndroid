package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/9.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class CloudHistoryListData(
    val playHistoryAnimes: MutableList<AnimeData> = mutableListOf()
) : CommonJsonData(), Parcelable