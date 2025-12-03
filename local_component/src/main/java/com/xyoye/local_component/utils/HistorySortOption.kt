package com.xyoye.local_component.utils

import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.utils.comparator.FileNameComparator
import com.xyoye.common_component.utils.comparator.FileTimeComparator
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.HistorySort

/**
 * Created by xyoye on 2023/4/11
 */

class HistorySortOption {
    var sort = HistorySort.TIME
        private set
    var asc = false
        private set

    init {
        sort = HistorySort.formValue(AppConfig.getHistorySortType())
        asc = AppConfig.getHistorySortAsc()
    }

    fun setSort(sort: HistorySort): Boolean {
        this.sort = sort
        AppConfig.setHistorySortType(sort.value)
        return true
    }

    fun changeAsc(): Boolean {
        this.asc = !asc
        AppConfig.setHistorySortAsc(asc)
        return true
    }

    fun createComparator(): Comparator<PlayHistoryEntity> {
        return if (sort == HistorySort.NAME) {
            FileNameComparator(
                getName = { it.videoName },
                isDirectory = { false },
                asc = asc,
                directoryFirst = false
            )
        } else {
            FileTimeComparator(
                getName = { it.videoName },
                getTime = { it.playTime },
                isDirectory = { false },
                asc = asc,
                directoryFirst = false
            )
        }
    }
}