package com.xyoye.anime_component.ui.activities.anime_follow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.data_component.data.FollowAnimeData

class AnimeFollowViewModel : BaseViewModel() {
    val followLiveData = MutableLiveData<FollowAnimeData>()

    fun getUserFollow() {
        httpRequest<FollowAnimeData>(viewModelScope) {
            onStart { showLoading() }

            api {
                Retrofit.service.getFollowAnime()
            }

            onError { showNetworkError(it) }

            onSuccess {
                followLiveData.postValue(it)
            }

            onComplete { hideLoading() }
        }
    }
}