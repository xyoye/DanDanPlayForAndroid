package com.xyoye.local_component.ui.activities.play_history

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayHistoryViewModel : BaseViewModel() {

    val showAddButton = ObservableBoolean()

    lateinit var playHistoryLiveData: LiveData<MutableList<PlayHistoryEntity>>
    val playLiveData = MutableLiveData<Any>()

    fun initHistoryType(mediaType: MediaType) {
        playHistoryLiveData = if (mediaType == MediaType.OTHER_STORAGE) {
            DatabaseManager.instance.getPlayHistoryDao().getAll()
        } else {
            DatabaseManager.instance.getPlayHistoryDao().getSingleMediaType(mediaType)
        }
    }

    fun removeHistory(history: PlayHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseManager.instance.getPlayHistoryDao().delete(history.id)
        }
    }

    fun clearHistory(mediaType: MediaType) {
        viewModelScope.launch(Dispatchers.IO) {
            val historyDao = DatabaseManager.instance.getPlayHistoryDao()
            if (mediaType == MediaType.STREAM_LINK || mediaType == MediaType.MAGNET_LINK) {
                historyDao.deleteTypeAll(listOf(mediaType))
            } else {
                historyDao.deleteTypeAll()
            }
        }
    }

    fun unbindDanmu(history: PlayHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            history.danmuPath = null
            history.episodeId = 0
            DatabaseManager.instance.getPlayHistoryDao().insert(history)
        }
    }

    fun unbindSubtitle(history: PlayHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            history.subtitlePath = null
            DatabaseManager.instance.getPlayHistoryDao().insert(history)
        }
    }

    fun openHistory(history: PlayHistoryEntity) {
        viewModelScope.launch {
            showLoading()
            val mediaSource = if (
                history.mediaType == MediaType.MAGNET_LINK && history.torrentPath != null
            ) {
                VideoSourceFactory.Builder()
                    .setRootPath(history.torrentPath!!)
                    .setIndex(history.torrentIndex)
                    .create(MediaType.MAGNET_LINK)
            } else {
                VideoSourceFactory.Builder()
                    .setVideoSources(listOf(history))
                    .createHistory()
            }
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("播放失败，无法打开播放资源")
                return@launch
            }

            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }

    fun openStreamLink(url: String, headers: Map<String, String>?) {
        viewModelScope.launch {
            showLoading()
            val mediaSource = VideoSourceFactory.Builder()
                .setVideoSources(listOf(url))
                .setHttpHeaders(headers ?: emptyMap())
                .create(MediaType.STREAM_LINK)
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("播放失败，无法打开播放资源")
                return@launch
            }
            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }
}