package com.xyoye.anime_component.ui.activities.anime_follow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.data_component.data.FollowAnimeData
import kotlinx.coroutines.launch

class AnimeFollowViewModel : BaseViewModel() {
    val followLiveData = MutableLiveData<FollowAnimeData>()

    fun getUserFollow() {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.getFollowedAnime()
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            if (result.isSuccess) {
                followLiveData.postValue(result.getOrThrow())
            }
        }
    }
}