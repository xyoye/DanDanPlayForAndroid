package com.xyoye.stream_component.ui.activities.remote_login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.helper.RemoteInterceptor
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class RemoteLoginViewModel : BaseViewModel() {
    val testConnectLiveData = MutableLiveData<Boolean>()

    fun testConnect(remoteData: MediaLibraryEntity) {
        val remoteUrl = "http://${remoteData.url}:${remoteData.port}/"
        val remoteToken = remoteData.remoteSecret
        RemoteInterceptor.getInstance().remoteUrl = remoteUrl
        RemoteInterceptor.getInstance().remoteToken = remoteToken

        httpRequest<ResponseBody>(viewModelScope) {

            onStart { showLoading() }

            api {
                Retrofit.remoteService.test()
            }

            onSuccess {
                testConnectLiveData.postValue(true)
                ToastCenter.showSuccess("连接成功")
            }

            onError {
                val errorMsg = if (it.code == 401) {
                    "连接失败：密钥验证失败"
                } else {
                    "连接失败：${it.message}"
                }

                ToastCenter.showWarning(errorMsg)
                testConnectLiveData.postValue(false)
            }

            onComplete { hideLoading() }
        }
    }

    fun addRemoteStorage(originalData: MediaLibraryEntity?, remoteData: MediaLibraryEntity) {
        viewModelScope.launch {
            if (originalData != null) {
                DatabaseManager.instance.getMediaLibraryDao()
                    .delete(originalData.url, originalData.mediaType)
            }

            if (remoteData.displayName.isEmpty()) {
                remoteData.displayName = "远程媒体库"
            }
            remoteData.describe = "http://${remoteData.url}:${remoteData.port}"

            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(remoteData)
        }
    }
}