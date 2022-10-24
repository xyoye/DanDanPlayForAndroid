package com.xyoye.common_component.bridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xyoye.data_component.entity.MediaLibraryEntity

/**
 * Created by xyoye on 2022/9/18.
 */

object ServiceLifecycleBridge {
    private val screencastReceiveLiveData = MutableLiveData<Boolean>()
    private val screencastProvideLiveData = MutableLiveData<MediaLibraryEntity?>()

    fun getScreencastReceiveObserver(): LiveData<Boolean> {
        return screencastReceiveLiveData
    }

    fun onScreencastReceiveLifeChange(alive: Boolean) {
        screencastReceiveLiveData.postValue(alive)
    }

    fun getScreencastProvideLiveData(): LiveData<MediaLibraryEntity?> {
        return screencastProvideLiveData
    }

    fun onScreencastProvideLifeChange(receiver: MediaLibraryEntity? = null) {
        screencastProvideLiveData.postValue(receiver)
    }

}