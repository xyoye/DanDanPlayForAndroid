package com.xyoye.anime_component.ui.fragment.anime_episode

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.collectable
import com.xyoye.common_component.extension.toText
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.StorageFactory
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.BangumiData
import com.xyoye.data_component.data.EpisodeData
import com.xyoye.data_component.entity.EpisodeHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class AnimeEpisodeFragmentViewModel : BaseViewModel() {
    private val utcTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    val animeTitleField = ObservableField<String>()
    val animeSearchWordField = ObservableField<String>()
    val episodeCountField = ObservableField<String>()

    // 是否升序
    private val _ascendingFlow = MutableStateFlow(true)
    val ascendingFlow = _ascendingFlow.collectable

    // 是否是标记模式
    private val _markModeFlow = MutableStateFlow(false)
    val markModeFlow = _markModeFlow.collectable

    // 已被标记的剧集ID列表
    private val _markedEpisodeIdsFlow = MutableStateFlow<List<String>>(emptyList())

    // 原始剧集数据
    private val _episodeListFlow = MutableStateFlow<List<EpisodeData>>(emptyList())

    // 剧集相关的本地播放记录数据
    @OptIn(FlowPreview::class)
    private val _episodeHistoryFlow = _episodeListFlow.flatMapConcat { episodes ->
        DatabaseManager.instance.getPlayHistoryDao().getEpisodeHistory(episodes.map { it.episodeId })
    }

    // 补全数据后的的剧集数据
    private val _formattedEpisodeFlow = _episodeListFlow.map { episodes ->
        episodes.map { completeEpisodeInfo(it) }
    }.combine(_episodeHistoryFlow) { episodes, histories ->
        completeEpisodeHistory(episodes, histories)
    }

    // 组合后的剧集列表
    val displayEpisodesFlow = combine(
        _formattedEpisodeFlow, _ascendingFlow, _markModeFlow, _markedEpisodeIdsFlow
    ) { episodes, ascending, markMode, markedEpisodeIds ->
        return@combine combineEpisodeData(episodes, ascending, markMode, markedEpisodeIds)
    }.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    // 刷新番剧信息
    private val _refreshBangumiFlow = MutableSharedFlow<Any>()
    val refreshBangumiFlow = _refreshBangumiFlow.collectable

    // 播放视频
    private val _playVideoFlow = MutableSharedFlow<Any>()
    val playVideoFLow = _playVideoFlow.collectable

    fun setBangumiData(bangumiData: BangumiData) {
        animeTitleField.set(bangumiData.animeTitle)
        animeSearchWordField.set(bangumiData.searchKeyword)
        episodeCountField.set("共${bangumiData.episodes.size}集")

        _episodeListFlow.value = bangumiData.episodes
    }

    /**
     * 补全剧集信息
     */
    private fun completeEpisodeInfo(episode: EpisodeData): EpisodeData {
        val titles = parseEpisodeTitles(episode.episodeTitle)
        return episode.copy(
            title = titles.first,
            subtitle = titles.second,
            searchEpisodeNum = getEpisodeNum(titles.first)
        )
    }

    /**
     * 补全剧集本地播放记录
     */
    private fun completeEpisodeHistory(
        episodes: List<EpisodeData>,
        histories: List<EpisodeHistoryEntity>
    ): List<EpisodeData> {
        val animeHistories = histories.groupBy { it.entity.episodeId }
        return episodes.map { episode ->
            val episodeHistories = animeHistories[episode.episodeId] ?: emptyList()

            // 获取云端与本地中最后的观看时间
            val watchTime = episodeHistories
                .map { it.entity.playTime }
                .plus(getCloudPlayTime(episode))
                .filterNotNull()
                .maxOfOrNull { it }
                ?.toText()
            episode.copy(histories = episodeHistories, watchTime = watchTime)
        }
    }

    /**
     * 组合剧集数据与展示状态控制
     */
    private fun combineEpisodeData(
        episodes: List<EpisodeData>,
        ascending: Boolean,
        markMode: Boolean,
        markedEpisodeIds: List<String>
    ): List<EpisodeData> {
        return (if (ascending) episodes else episodes.asReversed())
            .filter {
                markMode.not() || (markMode && it.markAble)
            }.map {
                it.copy(
                    inMarkMode = markMode,
                    isMarked = markedEpisodeIds.contains(it.episodeId)
                )
            }
    }

    /**
     * 提取剧集名称及话数
     */
    private fun parseEpisodeTitles(info: String): Pair<String, String> {
        val titles = info.split("\\s".toRegex())
        if (titles.isEmpty()) {
            return "第1话" to "未知剧集"
        }
        if (titles.size == 1) {
            return titles[0] to "未知剧集"
        }
        val title = titles[0]
        val subtitle = info.substring(title.length + 1)
        return title to subtitle
    }

    /**
     * 获取剧集的云端观看时间
     */
    private fun getCloudPlayTime(episode: EpisodeData): Date? {
        val time = episode.lastWatched
        if (time.isNullOrEmpty()) {
            return null
        }
        return try {
            utcTimeFormat.parse(time)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 提取剧集中的集数，用于搜索
     *
     * 例：第5话  ->  5
     */
    private fun getEpisodeNum(episodeText: String?): String {
        if (episodeText == null)
            return ""
        val pattern = Pattern.compile("第(\\d+)话")
        val matcher = pattern.matcher(episodeText)
        if (matcher.find()) {
            return matcher.group().run {
                var episode = substring(1, length - 1)
                if (episode.length == 1) {
                    episode = "0$episode"
                }
                episode
            }
        }
        return ""
    }

    /**
     * 切换排序方式
     */
    fun toggleSort() {
        _ascendingFlow.value = _ascendingFlow.value.not()
    }

    /**
     * 切换标记模式
     */
    fun toggleMarkMode(markMode: Boolean) {
        if (markMode) {
            val markAbleCount = _episodeListFlow.value.count { it.markAble }
            if (markAbleCount == 0) {
                "没有需标记为已看的剧集".toastError()
                return
            }
        }

        _markModeFlow.value = markMode
        if (markMode.not()) {
            _markedEpisodeIdsFlow.value = emptyList()
        }
    }

    /**
     * 播放与剧集相关的本地媒体库资源
     */
    fun playLocalEpisode(episodeHistory: EpisodeHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupHistorySource(episodeHistory)) {
                _playVideoFlow.emit(Any())
            }
        }
    }

    /**
     * 设置播放资源
     */
    private suspend fun setupHistorySource(episodeHistory: EpisodeHistoryEntity): Boolean {
        showLoading()
        val mediaSource = episodeHistory.library
            ?.let { StorageFactory.createStorage(it) }
            ?.run { historyFile(episodeHistory.entity) }
            ?.let { StorageVideoSourceFactory.create(it) }
        hideLoading()

        if (mediaSource == null) {
            ToastCenter.showError("播放失败，无法连接到播放资源")
            return false
        }
        VideoSourceManager.getInstance().setSource(mediaSource)
        return true
    }

    /**
     * 标记所有剧集
     */
    fun markAllEpisode() {
        val markedIds = _episodeListFlow.value
            .filter { it.markAble }
            .map { it.episodeId }
        _markedEpisodeIdsFlow.value = markedIds
    }

    /**
     * 反选剧集标记
     */
    fun reverseEpisodeMark() {
        val currentMarkedIds = _markedEpisodeIdsFlow.value
        val newMarkedIds = _episodeListFlow.value
            .filter { it.markAble && currentMarkedIds.contains(it.episodeId).not() }
            .map { it.episodeId }
        _markedEpisodeIdsFlow.value = newMarkedIds
    }

    /**
     * 标记剧集
     */
    fun markEpisodeById(episodeId: String) {
        val markedIds = _markedEpisodeIdsFlow.value.toMutableList()
        if (markedIds.contains(episodeId)) {
            markedIds.remove(episodeId)
        } else {
            markedIds.add(episodeId)
        }
        _markedEpisodeIdsFlow.value = markedIds
    }

    /**
     * 提交已标记的剧集列表为已观看
     */
    fun submitMarkedEpisodesViewed() {
        val markedIds = _markedEpisodeIdsFlow.value
        if (markedIds.isEmpty()) {
            "未选中需要标记为已看的剧集".toastError()
            return
        }
        submitEpisodesViewed(markedIds)
    }

    /**
     * 提交剧集列表为已观看
     */
    fun submitEpisodesViewed(episodeIds: List<String>) {
        viewModelScope.launch {
            showLoading()
            val result = AnimeRepository.addEpisodePlayHistory(episodeIds)
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            _markModeFlow.emit(false)
            _markedEpisodeIdsFlow.emit(emptyList())
            _refreshBangumiFlow.emit(Any())
        }
    }

}