package com.xyoye.anime_component.ui.activities.anime_follow

import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.data_component.data.AnimeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AnimeFollowViewModel : BaseViewModel() {
    private val _followedFlow = MutableStateFlow<List<AnimeData>>(emptyList())
    private val _searchWordFlow = MutableStateFlow("")

    val displayFollowedFlow = combine(_followedFlow, _searchWordFlow) { followed, searchWord ->
        combineAnimeFilter(followed, searchWord)
    }

    fun getUserFollow() {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.getFollowedAnime()
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            _followedFlow.emit(result.getOrNull()?.favorites ?: emptyList())
        }
    }

    fun searchAnime(keyword: String) {
        _searchWordFlow.value = keyword
    }

    private fun combineAnimeFilter(
        followed: List<AnimeData>,
        searchWord: String
    ): List<AnimeData> {
        if (searchWord.isEmpty()) {
            return followed
        }

        return followed.filter { it.animeTitle?.contains(searchWord, ignoreCase = true) == true }
    }
}