package com.xyoye.anime_component.ui.activities.anime_history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.data_component.data.CloudHistoryListData

class AnimeHistoryViewModel : BaseViewModel() {
    val historyLiveData = MutableLiveData<CloudHistoryListData>()

    fun getUserFollow() {
        httpRequest<CloudHistoryListData>(viewModelScope) {
            onStart { showLoading() }

            api {
                Retrofit.service.getCloudHistory()
            }

            onError { showNetworkError(it) }

            onSuccess {
                historyLiveData.postValue(it)
            }

            onComplete { hideLoading() }
        }
    }
}