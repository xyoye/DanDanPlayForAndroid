package com.xyoye.stream_component.ui.activities.storage_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageFileViewModel : BaseViewModel() {
    val playLiveData = MutableLiveData<Any>()
    val castLiveData = MutableLiveData<MediaLibraryEntity>()

    val selectDeviceLiveData = MutableLiveData<Pair<StorageFile, List<MediaLibraryEntity>>>()

    fun playItem(storage: Storage, file: StorageFile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupVideoSource(storage, file)) {
                playLiveData.postValue(Any())
            }
        }
    }

    fun castItem(storage: Storage, file: StorageFile) {
        viewModelScope.launch(Dispatchers.IO) {
            //获取所有可用的投屏设备
            val devices = DatabaseManager.instance.getMediaLibraryDao()
                .getByMediaTypeSuspend(MediaType.SCREEN_CAST)

            if (devices.isEmpty()) {
                ToastCenter.showError("无可用投屏设备")
                return@launch
            }
            if (devices.size == 1) {
                castItem(storage, file, devices.first())
                return@launch
            }

            selectDeviceLiveData.postValue(file to devices)
        }
    }

    fun castItem(storage: Storage, file: StorageFile, device: MediaLibraryEntity) {
        viewModelScope.launch {
            if (setupVideoSource(storage, file)) {
                castLiveData.postValue(device)
            }
        }
    }

    fun quicklyPlay(storage: Storage) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = DatabaseManager.instance.getPlayHistoryDao().gitStorageLastPlay(
                storageId = storage.library.id
            )
            if (history == null) {
                ToastCenter.showError("当前媒体库暂无播放记录")
                return@launch
            }

            val storageFile = storage.historyFile(history)
            if (storageFile == null) {
                ToastCenter.showError("播放失败，找不到上一次观看记录")
                return@launch
            }

            playItem(storage, storageFile)
        }
    }

    private suspend fun setupVideoSource(storage: Storage, file: StorageFile): Boolean {
        showLoading()
        val mediaSource = StorageVideoSourceFactory.create(file, storage)
        hideLoading()

        if (mediaSource == null) {
            ToastCenter.showError("播放失败，找不到播放资源")
            return false
        }
        VideoSourceManager.getInstance().setSource(mediaSource)
        return true
    }
}