package com.xyoye.stream_component.ui.activities.external_storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.launch

class ExternalStorageViewModel : BaseViewModel() {
    private val _exitLiveData = MutableLiveData<Any>()
    var exitLiveData: LiveData<Any> = _exitLiveData

    fun addExternalStorage(libraryEntity: MediaLibraryEntity) {
        viewModelScope.launch {
            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(libraryEntity)
            _exitLiveData.postValue(Any())
        }
    }
}