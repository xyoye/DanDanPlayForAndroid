package com.xyoye.local_component.ui.activities.play_history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.StorageFactory
import com.xyoye.common_component.storage.impl.LinkStorage
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.utils.HistorySortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayHistoryViewModel : BaseViewModel() {

    private val _historyLiveData = MutableLiveData<List<PlayHistoryEntity>>()
    val historyLiveData: LiveData<List<PlayHistoryEntity>> = _historyLiveData
    val playLiveData = MutableLiveData<Any>()

    // 文件排序选项
    private var sortOption = HistorySortOption()

    fun updatePlayHistory(mediaType: MediaType) {
        viewModelScope.launch {
            val historyData = if (mediaType == MediaType.OTHER_STORAGE) {
                DatabaseManager.instance.getPlayHistoryDao().getAll()
            } else {
                DatabaseManager.instance.getPlayHistoryDao().getSingleMediaType(mediaType)
            }
            _historyLiveData.postValue(historyData)
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
                historyDao.deleteAll()
            }
        }
    }

    /**
     * 修改文件排序
     */
    fun changeSortOption(option: HistorySortOption) {
        sortOption = option
        viewModelScope.launch(Dispatchers.IO) {
            val currentFiles = _historyLiveData.value ?: return@launch
            mutableListOf<PlayHistoryEntity>()
                .plus(currentFiles)
                .sortedWith(sortOption.createComparator())
                .apply { _historyLiveData.postValue(this) }
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
        viewModelScope.launch(Dispatchers.IO) {
            if (setupHistorySource(history)) {
                playLiveData.postValue(Any())
            }
        }
    }

    fun openStreamLink(link: String, headers: Map<String, String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupLinkSource(link, headers)) {
                playLiveData.postValue(Any())
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

    private suspend fun setupLinkSource(link: String, headers: Map<String, String>?): Boolean {
        showLoading()
        val mediaSource = MediaLibraryEntity.STREAM.copy(url = link)
            .run { StorageFactory.createStorage(this) }
            ?.run { this as? LinkStorage }
            ?.apply { this.setupHttpHeader(headers) }
            ?.run { getRootFile() }
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