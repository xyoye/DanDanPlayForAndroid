package com.xyoye.data_component.bean

import android.os.Parcelable
import com.xyoye.data_component.data.AnimeData
import com.xyoye.data_component.data.CloudHistoryData
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/9.
 */

@Parcelize
data class UserRelationBean(
    val followList: MutableList<AnimeData> = mutableListOf(),
    val historyList: MutableList<CloudHistoryData> = mutableListOf()
) : Parcelable