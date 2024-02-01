package com.xyoye.anime_component.ui.fragment.anime_episode

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.collectable
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.StorageFactory
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.BangumiData
import com.xyoye.data_component.data.EpisodeData
import com.xyoye.data_component.entity.PlayHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AnimeEpisodeFragmentViewModel : BaseViewModel() {
    val utcTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    val animeTitleField = ObservableField<String>()
    val animeSearchWordField = ObservableField<String>()
    val episodeCountField = ObservableField<String>()

    val episodeLiveData = MutableLiveData<List<EpisodeData>>()
    val episodeSortLiveData = MutableLiveData<Any>()

    private val _playVideoFlow = MutableSharedFlow<Any>()
    val playVideoFLow = _playVideoFlow.collectable

    private lateinit var bangumiData: BangumiData

    fun setBangumiData(bangumiData: BangumiData) {
        this.bangumiData = bangumiData
        animeTitleField.set(bangumiData.animeTitle)
        animeSearchWordField.set(bangumiData.searchKeyword)

        episodeCountField.set("共${bangumiData.episodes.size}集")

        episodeLiveData.postValue(bangumiData.episodes)

        refreshEpisodeHistory()
    }

    fun changeSort() {
        episodeSortLiveData.postValue(Any())
    }

    fun submitEpisodeRead(episodeId: List<String>) {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.addEpisodePlayHistory(episodeId)

            if (result.isFailure) {
                hideLoading()
                result.exceptionOrNull()?.message?.toastError()
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

        if (result.isFailure) {
            result.exceptionOrNull()?.message?.toastError()
            return
        }

        val episodes = result.getOrNull()?.bangumi?.episodes ?: emptyList()
        episodeLiveData.postValue(episodes)
    }

    fun refreshEpisodeHistory() {
        val episodeIds = episodeLiveData.value?.map { it.episodeId }
        if (episodeIds.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            DatabaseManager.instance.getPlayHistoryDao().getEpisodeHistory(episodeIds)
                .filter { it.library != null }
                .groupBy { it.entity.episodeId }
                .let { group ->
                    episodeLiveData.value?.map { episode ->
                        val histories = group[episode.episodeId] ?: emptyList()
                        val lastPlayTime = histories.maxByOrNull { it.entity.playTime.time }
                            ?.let { utcTimeFormat.format(it.entity.playTime) }
                            ?: episode.lastWatched
                        episode.copy(histories = histories, lastWatched = lastPlayTime)
                    }
                }?.let {
                    episodeLiveData.postValue(it)
                }
        }
    }

    fun playEpisodeHistory(history: PlayHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupHistorySource(history)) {
                _playVideoFlow.emit(Any())
            }
        }
    }

    private suspend fun setupHistorySource(history: PlayHistoryEntity): Boolean {
        showLoading()
        val mediaSource = history.storageId
            ?.run { DatabaseManager.instance.getMediaLibraryDao().getById(this) }
            ?.run { StorageFactory.createStorage(this) }
            ?.run { historyFile(history) }
            ?.run { StorageVideoSourceFactory.create(this) }
        hideLoading()

        if (mediaSource == null) {
            ToastCenter.showError("播放失败，找不到播放资源")
            return false
        }
        VideoSourceManager.getInstance().setSource(mediaSource)
        return true
    }

}