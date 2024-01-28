package com.xyoye.anime_component.ui.fragment.anime_episode

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.BangumiData
import com.xyoye.data_component.data.EpisodeData
import kotlinx.coroutines.launch

class AnimeEpisodeFragmentViewModel : BaseViewModel() {
    val animeTitleField = ObservableField<String>()
    val animeSearchWordField = ObservableField<String>()
    val episodeCountField = ObservableField<String>()

    val episodeLiveData = MutableLiveData<List<EpisodeData>>()
    val episodeSortLiveData = MutableLiveData<Any>()

    private lateinit var bangumiData: BangumiData

    fun setBangumiData(bangumiData: BangumiData) {
        this.bangumiData = bangumiData
        animeTitleField.set(bangumiData.animeTitle)
        animeSearchWordField.set(bangumiData.searchKeyword)

        episodeCountField.set("共${bangumiData.episodes.size}集")

        episodeLiveData.postValue(bangumiData.episodes)
    }

    fun changeSort() {
        episodeSortLiveData.postValue(Any())
    }

    fun submitEpisodeRead(episodeId: List<String>) {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.addEpisodePlayHistory(episodeId)

            if (result is Response.Error) {
                hideLoading()
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            refreshEpisode()
            hideLoading()
        }
    }

    private suspend fun refreshEpisode() {
        if (this::bangumiData.isInitialized.not()) {
            return
        }

        val result = AnimeRepository.getAnimeDetail(bangumiData.animeId.toString())

        if (result is Response.Error) {
            ToastCenter.showError(result.error.toastMsg)
            return
        }

        val episodes = result.dataOrNull?.bangumi?.episodes ?: emptyList()
        episodeLiveData.postValue(episodes)
    }
}