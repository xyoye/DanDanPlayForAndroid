package com.xyoye.anime_component.ui.fragment.anime_recommend

import androidx.databinding.ObservableField
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.data_component.data.BangumiData

class AnimeRecommendFragmentViewModel : BaseViewModel() {
    val hideRecommendFiled = ObservableField(true)
    val hideRecommendMoreFiled = ObservableField(true)

    fun setBangumiData(bangumiData: BangumiData) {
        hideRecommendFiled.set(bangumiData.relateds.isEmpty())
        hideRecommendMoreFiled.set(bangumiData.similars.isEmpty())
    }
}