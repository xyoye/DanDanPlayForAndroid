package com.xyoye.anime_component.ui.fragment.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.network.repository.OtherRepository
import com.xyoye.data_component.data.BangumiAnimeData
import com.xyoye.data_component.data.BannerData
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2020/7/28.
 */

class HomeFragmentViewModel : BaseViewModel() {
    val tabTitles = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    val weeklyAnimeLiveData = MutableLiveData<Array<BangumiAnimeData>>()
    val bannersLiveData = MutableLiveData<BannerData>()

    fun getWeeklyAnime() {
        viewModelScope.launch {
            val result = AnimeRepository.getWeeklyAnime()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            result.getOrNull()?.let {
                weeklyAnimeLiveData.postValue(splitWeeklyAnime(it))
            }
        }
    }

    fun getBanners() {
        viewModelScope.launch {
            val result = OtherRepository.getHomeBanner()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            result.getOrNull()?.let { bannersLiveData.postValue(it) }
        }
    }

    /**
     * 按日期分割数据
     */
    private fun splitWeeklyAnime(weeklyAnimeData: BangumiAnimeData): Array<BangumiAnimeData> {
        val weeklyAnimeDataArray = Array(7) { BangumiAnimeData() }
        weeklyAnimeData.bangumiList
            .filter { it.airDay <= 7 }
            .groupBy { it.airDay }
            .entries
            .forEach {
                if (it.key < weeklyAnimeDataArray.size) {
                    weeklyAnimeDataArray[it.key] = BangumiAnimeData(it.value.toMutableList())
                }
            }
        return weeklyAnimeDataArray
    }
}