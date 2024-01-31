package com.xyoye.anime_component.ui.activities.anime_tag

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.data_component.data.AnimeTagData
import kotlinx.coroutines.launch

class AnimeTagViewModel : BaseViewModel() {
    val tagAnimeLiveData = MutableLiveData<AnimeTagData>()

    fun getAnimeByTag(tagId: Int) {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.searchAnimeByTag(listOf(tagId.toString()))
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
            }

            if (result.isSuccess) {
                tagAnimeLiveData.postValue(result.getOrThrow())
            }
        }
    }
}