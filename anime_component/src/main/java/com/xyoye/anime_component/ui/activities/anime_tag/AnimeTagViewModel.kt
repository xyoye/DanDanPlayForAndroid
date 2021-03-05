package com.xyoye.anime_component.ui.activities.anime_tag

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.data_component.data.AnimeTagData

class AnimeTagViewModel : BaseViewModel() {
    val tagAnimeLiveData = MutableLiveData<AnimeTagData>()

    fun getAnimeByTag(tagId: Int) {
        httpRequest<AnimeTagData>(viewModelScope) {
            onStart { showLoading() }

            api {
                Retrofit.service.searchByTag(tagId.toString())
            }

            onError { showNetworkError(it) }

            onSuccess {
                tagAnimeLiveData.postValue(it)
            }

            onComplete { hideLoading() }
        }
    }
}