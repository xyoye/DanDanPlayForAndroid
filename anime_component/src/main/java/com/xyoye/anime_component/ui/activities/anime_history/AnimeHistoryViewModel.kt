package com.xyoye.anime_component.ui.activities.anime_history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.CloudHistoryListData
import kotlinx.coroutines.launch

class AnimeHistoryViewModel : BaseViewModel() {
    val historyLiveData = MutableLiveData<CloudHistoryListData>()

    fun getCloudHistory() {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.getPlayHistory()
            hideLoading()

            if (result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            if (result is Response.Success) {
                historyLiveData.postValue(result.data)
            }

        }
    }
}