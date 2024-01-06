package com.xyoye.anime_component.ui.fragment.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.repository.DanDanPlayRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.httpRequest
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
            val result = DanDanPlayRepository.getWeeklyAnime()

            if (result is Response.Error) {
                showNetworkError(result.error)
                return@launch
            }

            if (result is Response.Success) {
                val weeklyAnimeData = splitWeeklyAnime(result.data)
                weeklyAnimeLiveData.postValue(weeklyAnimeData)
            }
        }
    }

    fun getBanners() {
        httpRequest<BannerData>(viewModelScope) {
            api {
                Retrofit.service.getBanners()
            }
            onSuccess {
                bannersLiveData.postValue(it)
            }

            onError {
                showNetworkError(it)
            }
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