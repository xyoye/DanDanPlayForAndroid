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
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteFileFragmentViewModel : BaseViewModel() {

    val fileLiveData = MutableLiveData<List<StorageFileBean>>()
    val playLiveData = MutableLiveData<Any>()

    val curDirectoryFiles = mutableListOf<RemoteVideoData>()

    fun initDirectoryFiles(fileList: MutableList<RemoteVideoData>?) {
        curDirectoryFiles.clear()
        fileList?.let {
            curDirectoryFiles.addAll(it)
        }
        refreshDirectoryWithHistory()
    }

    fun refreshDirectoryWithHistory() {
        viewModelScope.launch {
            val storageFiles = curDirectoryFiles.map {
                val uniqueKey = RemoteSourceFactory.generateUniqueKey(it)
                val history = DatabaseManager.instance
                    .getPlayHistoryDao()
                    .getHistoryByKey(uniqueKey, MediaType.REMOTE_STORAGE)

                StorageFileBean(
                    it.isFolder,
                    it.absolutePath,
                    it.getEpisodeName(),
                    history?.danmuPath,
                    history?.subtitlePath,
                    history?.videoPosition ?: 0L,
                    history?.videoDuration ?: (it.Duration ?: 0L) * 1000,
                    uniqueKey,
                    it.childData.size,
                    RemoteHelper.getInstance().buildImageUrl(it.Id)
                )
            }
            fileLiveData.postValue(storageFiles)
        }
    }

    fun playItem(uniqueKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoSources = curDirectoryFiles.filter { isVideoFile(it.Name) }
            val index = videoSources.indexOfFirst {
                RemoteSourceFactory.generateUniqueKey(it) == uniqueKey
            }
            if (videoSources.isEmpty() || index < 0) {
                ToastCenter.showError("播放失败，不支持播放的资源")
                return@launch
            }

            showLoading()
            val mediaSource = VideoSourceFactory.Builder()
                .setVideoSources(videoSources)
                .setIndex(index)
                .create(MediaType.REMOTE_STORAGE)
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }

            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }
}