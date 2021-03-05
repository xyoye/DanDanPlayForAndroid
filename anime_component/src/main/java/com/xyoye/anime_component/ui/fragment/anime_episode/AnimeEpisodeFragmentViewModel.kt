package com.xyoye.anime_component.ui.fragment.anime_episode

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.data_component.data.BangumiData
import com.xyoye.data_component.data.EpisodeData

class AnimeEpisodeFragmentViewModel : BaseViewModel() {
    val animeTitleField = ObservableField<String>()
    val animeSearchWordField = ObservableField<String>()
    val episodeCountField = ObservableField<String>()

    val episodeLiveData = MutableLiveData<MutableList<EpisodeData>>()
    val episodeSortLiveData = MutableLiveData<Boolean>()

    private lateinit var bangumiData: BangumiData

    fun setBangumiData(bangumiData: BangumiData) {
        this.bangumiData = bangumiData
        animeTitleField.set(bangumiData.animeTitle)
        animeSearchWordField.set(bangumiData.searchKeyword)

        episodeCountField.set("共${bangumiData.episodes.size}集")

        episodeLiveData.postValue(bangumiData.episodes)
    }

    fun changeSort() {
        val asc = episodeSortLiveData.value ?: true
        episodeSortLiveData.postValue(!asc)
    }
}