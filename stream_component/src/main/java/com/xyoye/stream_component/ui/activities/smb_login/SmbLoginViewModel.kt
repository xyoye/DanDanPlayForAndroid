package com.xyoye.stream_component.ui.activities.smb_login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.utils.smb.v2.SMBJManager
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmbLoginViewModel : BaseViewModel() {

    val testConnectLiveData = MutableLiveData<Boolean>()

    fun testConnect(serverData: MediaLibraryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isSuccess = SMBJManager.getInstance().testConnect(serverData)
            testConnectLiveData.postValue(isSuccess)
        }
    }

    fun addWebDavStorage(originalData: MediaLibraryEntity?, serverData: MediaLibraryEntity) {
        viewModelScope.launch {
            if (originalData != null){
                DatabaseManager.instance.getMediaLibraryDao()
                    .delete(originalData.url, originalData.mediaType)
            }

            if (serverData.displayName.isEmpty()) {
                serverData.displayName = "SMB媒体库"
            }
            serverData.describe = "smb://${serverData.url}"

            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(serverData)
        }
    }
}