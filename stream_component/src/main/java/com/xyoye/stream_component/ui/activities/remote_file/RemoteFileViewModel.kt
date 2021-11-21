package com.xyoye.stream_component.ui.activities.remote_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.media.RemoteMediaSource
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.utils.remote.RemoteFileHelper
import kotlinx.coroutines.launch

class RemoteFileViewModel : BaseViewModel() {

    val playLiveData = MutableLiveData<Any>()
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
                RemoteFileHelper.convertTreeData(videoData)
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

    fun playIndexFromList(data: RemoteVideoData, videoList: List<RemoteVideoData>) {
        viewModelScope.launch {
            val videoSources = videoList.filter { it.isFolder.not() }
            val index = videoSources.indexOf(data)
            if (videoSources.isEmpty() || index < 0) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }

            showLoading()
            val mediaSource = RemoteMediaSource.build(
                index,
                videoSources
            )
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }

            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }
}