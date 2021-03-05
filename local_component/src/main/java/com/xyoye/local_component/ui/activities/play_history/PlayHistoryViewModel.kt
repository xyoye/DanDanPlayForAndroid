package com.xyoye.local_component.ui.activities.play_history

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayHistoryViewModel : BaseViewModel() {

    val showAddButton = ObservableBoolean()
    val isEditMode = ObservableBoolean()

    lateinit var playHistoryLiveData: LiveData<MutableList<PlayHistoryEntity>>

    fun initHistoryType(mediaType: MediaType) {
        playHistoryLiveData = if (mediaType == MediaType.OTHER_STORAGE) {
            val mediaTypes = arrayOf(
                MediaType.LOCAL_STORAGE,
                MediaType.OTHER_STORAGE,
                MediaType.FTP_SERVER,
                MediaType.SMB_SERVER,
                MediaType.WEBDAV_SERVER
            )
            DatabaseManager.instance.getPlayHistoryDao().getMultipleMediaType(mediaTypes)
        } else {
            DatabaseManager.instance.getPlayHistoryDao().getSingleMediaType(mediaType)
        }
    }

    fun removeHistory(historyList: MutableList<PlayHistoryEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val historyDao = DatabaseManager.instance.getPlayHistoryDao()
            historyList.forEach {
                historyDao.delete(it.url, it.mediaType)
            }
        }
    }

}