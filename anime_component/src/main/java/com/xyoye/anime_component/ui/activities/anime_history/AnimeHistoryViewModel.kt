package com.xyoye.anime_component.ui.activities.anime_history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.data_component.data.CloudHistoryListData
import kotlinx.coroutines.launch

class AnimeHistoryViewModel : BaseViewModel() {
    val historyLiveData = MutableLiveData<CloudHistoryListData>()

    fun getCloudHistory() {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.getPlayHistory()
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            result.getOrNull()?.let { historyLiveData.postValue(it) }
        }
    }
}