package com.xyoye.stream_component.ui.activities.web_dav_login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.helper.UnsafeOkHttpClient
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebDavLoginViewModel : BaseViewModel() {

    val testConnectLiveData = MutableLiveData<Boolean>()

    fun testConnect(serverData: MediaLibraryEntity) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val sardine = OkHttpSardine(UnsafeOkHttpClient.client)
            if (!serverData.account.isNullOrEmpty()) {
                sardine.setCredentials(serverData.account, serverData.password)
            }

            try {
                sardine.list(serverData.url)
                testConnectLiveData.postValue(true)
                ToastCenter.showSuccess("连接成功")
            } catch (t: Throwable) {
                val errorMsg = "连接失败：${t.message}"
                ToastCenter.showWarning(errorMsg)
                testConnectLiveData.postValue(false)
            }

            withContext(Dispatchers.Main) {
                hideLoading()
            }
        }
    }

    fun addWebDavStorage(originalData: MediaLibraryEntity?, serverData: MediaLibraryEntity) {
        viewModelScope.launch {
            if (originalData != null){
                DatabaseManager.instance.getMediaLibraryDao()
                    .delete(originalData.url, originalData.mediaType)
            }

            if (serverData.displayName.isEmpty()) {
                serverData.displayName = "WebDav媒体库"
            }
            serverData.describe = serverData.url

            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(serverData)
        }
    }
}