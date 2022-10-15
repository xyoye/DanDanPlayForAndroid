package com.xyoye.stream_component.ui.fragment.remote_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.factory.RemoteSourceFactory
import com.xyoye.common_component.utils.RemoteHelper
import com.xyoye.common_component.utils.isVideoFile
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.StorageFileBean
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RemoteFileFragmentViewModel : BaseViewModel() {

    val fileLiveData = MutableLiveData<List<StorageFileBean>>()
    val playLiveData = MutableLiveData<Any>()
    val castLiveData = MutableLiveData<MediaLibraryEntity>()

    val curDirectoryFiles = mutableListOf<RemoteVideoData>()

    private var refreshJob: Job? = null

    fun initDirectoryFiles(fileList: MutableList<RemoteVideoData>?) {
        curDirectoryFiles.clear()
        fileList?.let {
            curDirectoryFiles.addAll(it)
        }
        refreshDirectoryWithHistory()
    }

    fun refreshDirectoryWithHistory() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            val storageFiles = curDirectoryFiles.map {
                val uniqueKey = RemoteSourceFactory.generateUniqueKey(it)
                val history = DatabaseManager.instance
                    .getPlayHistoryDao()
                    .getPlayHistory(uniqueKey, MediaType.REMOTE_STORAGE)

                val historyDuration = history?.videoDuration ?: 0L
                val remoteDuration = it.Duration ?: 0L
                val duration = if (historyDuration > 0) {
                    historyDuration
                } else {
                    remoteDuration * 1000
                }

                StorageFileBean(
                    it.isFolder,
                    it.absolutePath,
                    it.getEpisodeName(),
                    history?.danmuPath,
                    history?.subtitlePath,
                    history?.videoPosition ?: 0L,
                    duration,
                    uniqueKey,
                    it.childData.size,
                    RemoteHelper.getInstance().buildImageUrl(it.Id),
                    lastPlayTime = history?.playTime
                )
            }
            fileLiveData.postValue(storageFiles)
        }
    }

    fun playItem(uniqueKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupVideoSource(uniqueKey)) {
                playLiveData.postValue(Any())
            }
        }
    }

    fun castItem(uniqueKey: String, device: MediaLibraryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupVideoSource(uniqueKey)) {
                castLiveData.postValue(device)
            }
        }
    }

    private suspend fun setupVideoSource(uniqueKey: String): Boolean {
        val videoSources = curDirectoryFiles.filter { isVideoFile(it.Name) }
        val index = videoSources.indexOfFirst {
            RemoteSourceFactory.generateUniqueKey(it) == uniqueKey
        }
        if (videoSources.isEmpty() || index < 0) {
            ToastCenter.showError("播放失败，不支持播放的资源")
            return false
        }

        showLoading()
        val mediaSource = VideoSourceFactory.Builder()
            .setVideoSources(videoSources)
            .setIndex(index)
            .create(MediaType.REMOTE_STORAGE)
        hideLoading()

        if (mediaSource == null) {
            ToastCenter.showError("播放失败，找不到播放资源")
            return false
        }
        VideoSourceManager.getInstance().setSource(mediaSource)
        return true
    }
}