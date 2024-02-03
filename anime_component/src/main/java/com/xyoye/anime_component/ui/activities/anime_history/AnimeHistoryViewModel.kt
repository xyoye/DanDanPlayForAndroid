package com.xyoye.anime_component.ui.activities.anime_history

import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.data_component.data.AnimeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AnimeHistoryViewModel : BaseViewModel() {
    private val _historiesFlow = MutableStateFlow<List<AnimeData>>(emptyList())
    private val _searchWordFlow = MutableStateFlow("")

    val displayHistoriesFlow = combine(_historiesFlow, _searchWordFlow) { histories, searchWord ->
        combineAnimeFilter(histories, searchWord)
    }

    fun getCloudHistory() {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.getPlayHistory()
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            _historiesFlow.emit(result.getOrNull()?.playHistoryAnimes ?: emptyList())
        }
    }

    fun searchAnime(keyword: String) {
        _searchWordFlow.value = keyword
    }

    private fun combineAnimeFilter(
        histories: List<AnimeData>,
        searchWord: String
    ): List<AnimeData> {
        if (searchWord.isEmpty()) {
            return histories
        }

        return histories.filter { it.animeTitle?.contains(searchWord, ignoreCase = true) == true }
    }
}