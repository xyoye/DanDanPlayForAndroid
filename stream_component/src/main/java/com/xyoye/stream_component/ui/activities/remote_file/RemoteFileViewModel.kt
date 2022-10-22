package com.xyoye.stream_component.ui.activities.remote_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.RemoteHelper
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.utils.remote.RemoteFileHelper

class RemoteFileViewModel : BaseViewModel() {

    val folderLiveData = MutableLiveData<MutableList<RemoteVideoData>>()

    fun openStorage(remoteData: MediaLibraryEntity) {
        val remoteUrl = "http://${remoteData.url}:${remoteData.port}/"
        val remoteToken = remoteData.remoteSecret
        RemoteHelper.getInstance().remoteUrl = remoteUrl
        RemoteHelper.getInstance().remoteToken = remoteToken

        httpRequest<MutableList<RemoteVideoData>>(viewModelScope) {

            onStart {
                showLoading()
            }

            api {
                val videoData = Retrofit.remoteService.openStorage()
                if (remoteData.remoteAnimeGrouping) {
                    RemoteFileHelper.convertGroupData(videoData)
                } else {
                    RemoteFileHelper.convertTreeData(videoData)
                }
            }

            onSuccess {
                folderLiveData.postValue(it)
            }

            onError {
                val errorMsg = if (it.code == 401) {
                    "连接失败：密钥验证失败"
                } else {
                    "连接失败：${it.msg}"
                }

                ToastCenter.showWarning(errorMsg)
            }

            onComplete {
                hideLoading()
            }
        }
    }
}