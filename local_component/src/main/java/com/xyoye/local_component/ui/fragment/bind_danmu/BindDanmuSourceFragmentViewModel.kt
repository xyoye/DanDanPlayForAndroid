package com.xyoye.local_component.ui.fragment.bind_danmu

import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.collectable
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.danmu.DanmuFinder
import com.xyoye.common_component.utils.danmu.source.DanmuSourceFactory
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.DanmuAnimeData
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.data.DanmuRelatedUrlData
import com.xyoye.data_component.entity.PlayHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * Created by xyoye on 2022/1/25
 */
class BindDanmuSourceFragmentViewModel : BaseViewModel() {

    private lateinit var storageFile: StorageFile

    private val _boundEpisodeFlow = MutableStateFlow<PlayHistoryEntity?>(null)
    private val _matchedAnimeFlow = MutableStateFlow<DanmuAnimeData?>(null)
    private val _selectedAnimeFlow = MutableStateFlow<DanmuAnimeData?>(null)
    private val _searchedAnimeFlow = MutableStateFlow<List<DanmuAnimeData>>(emptyList())

    private val _downloadDialogDismissFlow = MutableSharedFlow<Any>()
    val downloadDialogDismissFlow = _downloadDialogDismissFlow.collectable

    private val _downloadDialogShowFlow = MutableSharedFlow<Pair<DanmuEpisodeData, List<DanmuRelatedUrlData>>>()
    val downloadDialogShowFlow = _downloadDialogShowFlow.collectable

    val danmuAnimeListFlow = combine(
        _searchedAnimeFlow,
        _matchedAnimeFlow,
        _selectedAnimeFlow,
        _boundEpisodeFlow
    ) { search, matched, selected, bond ->
        mapDanmuAnimeList(search, matched, selected, bond)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val danmuEpisodeListFlow = combine(
        _selectedAnimeFlow,
        _boundEpisodeFlow
    ) { selected, bound ->
        mapDanmuEpisodeList(selected, bound)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setStorageFile(storageFile: StorageFile) {
        this.storageFile = storageFile

        viewModelScope.launch {
            _boundEpisodeFlow.emit(storageFile.playHistory)
        }
    }

    fun matchDanmu() {
        val danmuSource = DanmuSourceFactory.build(storageFile)
            ?: return

        viewModelScope.launch {
            showLoading()
            val matched = DanmuFinder.instance.getMatched(danmuSource)
            hideLoading()

            // 保存匹配结果
            _matchedAnimeFlow.emit(matched)
            // 选中匹配结果
            _selectedAnimeFlow.emit(matched)
        }
    }

    fun searchDanmu(searchText: String) {
        if (searchText.isEmpty())
            return

        viewModelScope.launch {
            showLoading()
            val result = DanmuFinder.instance.search(searchText)
            hideLoading()

            _searchedAnimeFlow.emit(result)
            // 自动选择第一个动画。
            if (result.isNotEmpty()) {
                _selectedAnimeFlow.emit(result[0])
            }
        }
    }

    fun getDanmuThirdSource(episode: DanmuEpisodeData) {
        viewModelScope.launch {
            showLoading()
            val result = DanmuFinder.instance.getRelated(episode.episodeId)
            hideLoading()

            _downloadDialogShowFlow.emit(episode to result)
        }
    }

    fun downloadDanmu(episode: DanmuEpisodeData, withRelated: Boolean = true) {
        viewModelScope.launch {
            showLoading()
            val localDanmu = DanmuFinder.instance.downloadEpisode(episode, withRelated)

            if (localDanmu == null) {
                hideLoading()
                ToastCenter.showError("保存弹幕失败")
                return@launch
            }

            databaseDanmu(localDanmu.danmuPath, localDanmu.episodeId)

            hideLoading()
            ToastCenter.showSuccess("保存弹幕成功！")
            _downloadDialogDismissFlow.emit(Any())
        }
    }

    fun downloadDanmu(episode: DanmuEpisodeData, related: List<DanmuRelatedUrlData>) {
        if (related.isEmpty()) {
            ToastCenter.showWarning("请至少选择一个弹幕源")
            return
        }

        viewModelScope.launch {
            showLoading()
            val localDanmu = DanmuFinder.instance.downloadRelated(episode, related)

            if (localDanmu == null) {
                hideLoading()
                ToastCenter.showError("保存弹幕失败")
                return@launch
            }

            databaseDanmu(localDanmu.danmuPath, localDanmu.episodeId)

            hideLoading()
            ToastCenter.showSuccess("保存弹幕成功！")
            _downloadDialogDismissFlow.emit(Any())
        }
    }

    fun selectAnime(anime: DanmuAnimeData) {
        viewModelScope.launch {
            _selectedAnimeFlow.emit(anime)
        }
    }

    fun unbindDanmu() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDanmu(null)
        }
    }

    fun bindLocalDanmu(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDanmu(filePath)
        }
    }

    private suspend fun databaseDanmu(danmuPath: String?, episodeId: String? = null) {
        val historyEntity = getStorageFileHistory().copy(
            danmuPath = danmuPath,
            episodeId = episodeId
        )
        DatabaseManager.instance.getPlayHistoryDao().insert(historyEntity)

        _boundEpisodeFlow.emit(historyEntity)
    }

    private suspend fun getStorageFileHistory(): PlayHistoryEntity {
        return DatabaseManager.instance.getPlayHistoryDao().getPlayHistory(
            storageFile.uniqueKey(),
            storageFile.storage.library.id
        ) ?: PlayHistoryEntity(
            0,
            "",
            "",
            mediaType = storageFile.storage.library.mediaType,
            uniqueKey = storageFile.uniqueKey(),
            storageId = storageFile.storage.library.id,
        )
    }

    private fun mapDanmuAnimeList(
        searched: List<DanmuAnimeData>,
        matched: DanmuAnimeData?,
        selected: DanmuAnimeData?,
        bound: PlayHistoryEntity?
    ): List<DanmuAnimeData> {
        val animeList = if (matched == null) searched else listOf(matched) + searched
        return animeList.map { anime ->
            anime.copy(
                isSelected = anime.animeId == selected?.animeId,
                isRecommend = anime.animeId == matched?.animeId,
                isBound = anime.episodes.any { it.episodeId == bound?.episodeId }
            )
        }
    }

    private fun mapDanmuEpisodeList(
        selected: DanmuAnimeData?,
        bound: PlayHistoryEntity?
    ): List<DanmuEpisodeData> {
        return selected?.episodes?.map { episode ->
            episode.copy(
                isBound = episode.episodeId == bound?.episodeId
            )
        } ?: emptyList()
    }
}