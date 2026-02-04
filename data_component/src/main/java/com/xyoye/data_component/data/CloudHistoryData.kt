package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2021/1/9.
 */

@Parcelize
@Serializable
data class CloudHistoryListData(
    val playHistoryAnimes: List<AnimeData> = emptyList()
) : CommonJsonData(), Parcelable