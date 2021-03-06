package com.xyoye.anime_component.ui.fragment.anime_recommend

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.data_component.data.AnimeData
import com.xyoye.data_component.data.BangumiData

class AnimeRecommendFragmentViewModel : BaseViewModel() {
    val hideRecommendFiled = ObservableField<Boolean>(true)
    val hideRecommendMoreFiled = ObservableField<Boolean>(true)

    val recommendLiveData = MutableLiveData<MutableList<AnimeData>>()
    val recommendMoreLiveData = MutableLiveData<MutableList<AnimeData>>()

    fun setBangumiData(bangumiData: BangumiData) {
        bangumiData.apply {
            if (relateds.size > 0) {
                hideRecommendFiled.set(false)
                recommendLiveData.postValue(relateds)
            }

            if (similars.size > 0) {
                hideRecommendMoreFiled.set(false)
                recommendMoreLiveData.postValue(similars)
            }
        }

    }
}