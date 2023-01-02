package com.xyoye.stream_component.ui.activities.storage_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.weight.ToastCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageFileViewModel : BaseViewModel() {
    val playLiveData = MutableLiveData<Any>()

    fun playItem(storage: Storage, file: StorageFile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupVideoSource(storage, file)) {
                playLiveData.postValue(Any())
            }
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