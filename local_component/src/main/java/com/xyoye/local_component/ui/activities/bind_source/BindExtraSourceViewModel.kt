package com.xyoye.local_component.ui.activities.bind_source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.storage.file.StorageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by xyoye on 2022/1/24
 */
class BindExtraSourceViewModel : BaseViewModel() {

    private val _historyChangedLiveData = MediatorLiveData<StorageFile>()
    val historyChangedLiveData: LiveData<StorageFile> = _historyChangedLiveData

    fun updateSourceChanged(storageFile: StorageFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = DatabaseManager.instance.getPlayHistoryDao().getPlayHistory(
                storageFile.uniqueKey(),
                storageFile.storage.library.id
            )
            val newStorageFile = storageFile.clone().apply {
                playHistory = history
            }
            _historyChangedLiveData.postValue(newStorageFile)
        }
    }
}