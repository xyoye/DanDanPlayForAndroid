package com.xyoye.stream_component.ui.activities.storage_plus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.storage.StorageFactory
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoragePlusViewModel : BaseViewModel() {
    private val _testLiveData = MutableLiveData<Boolean>()
    var testLiveData: LiveData<Boolean> = _testLiveData

    private val _exitLiveData = MutableLiveData<Any>()
    var exitLiveData: LiveData<Any> = _exitLiveData

    fun addStorage(oldLibrary: MediaLibraryEntity?, newLibrary: MediaLibraryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val duplicateLibrary = DatabaseManager.instance.getMediaLibraryDao()
                .getByUrl(newLibrary.url, newLibrary.mediaType)
            if (duplicateLibrary != null && duplicateLibrary.id != oldLibrary?.id) {
                ToastCenter.showError("保存失败，媒体库地址已存在")
                return@launch
            }

            newLibrary.id = oldLibrary?.id ?: 0
            DatabaseManager.instance.getMediaLibraryDao().insert(newLibrary)
            _exitLiveData.postValue(Any())
        }
    }

    fun testStorage(library: MediaLibraryEntity) {
        val storage = StorageFactory.createStorage(library)
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val status = storage?.test() ?: false
            hideLoading()
            _testLiveData.postValue(status)
        }
    }
}