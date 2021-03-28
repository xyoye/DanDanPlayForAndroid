package com.xyoye.stream_component.ui.activities.remote_login

import androidx.lifecycle.MutableLiveData
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.data_component.entity.MediaLibraryEntity

class RemoteLoginViewModel : BaseViewModel() {
    val testConnectLiveData = MutableLiveData<Boolean>()

    fun addRemoteStorage(originalData: MediaLibraryEntity?, remoteData: MediaLibraryEntity){

    }

    fun testConnect(remoteData: MediaLibraryEntity){

    }
}