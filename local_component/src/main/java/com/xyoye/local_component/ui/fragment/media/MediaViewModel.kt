package com.xyoye.local_component.ui.fragment.media

import android.provider.MediaStore
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.bridge.ServiceLifecycleBridge
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2020/7/27.
 */

class MediaViewModel : BaseViewModel() {

    val mediaLibWithStatusLiveData = MediatorLiveData<MutableList<MediaLibraryEntity>>().apply {
        val mediaLibrariesLiveData = DatabaseManager.instance.getMediaLibraryDao().getAll()
        val serviceStatusLiveData = ServiceLifecycleBridge.getScreencastProvideLiveData()
        //媒体库数据源
        addSource(mediaLibrariesLiveData) { libraries ->
            libraries.onEach {
                it.running =
                    it.mediaType == MediaType.SCREEN_CAST && it == serviceStatusLiveData.value
            }
            this.postValue(libraries)
        }
        //投屏服务状态数据源
        addSource(serviceStatusLiveData) { running ->
            val newData = this.value?.onEach {
                it.running = it.mediaType == MediaType.SCREEN_CAST && it == running
            } ?: mutableListOf()
            this.postValue(newData)
        }
    }

    fun initLocalStorage() {
        val localStorageEntity = MediaLibraryEntity(
            1,
            "本地媒体库",
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString(),
            MediaType.LOCAL_STORAGE
        )
        val streamEntity = MediaLibraryEntity(
            2,
            "串流播放",
            "url://dandanplay_steam_link",
            MediaType.STREAM_LINK,
            null,
            null,
            true,
            0,
            "https://"
        )
        val magnetEntity = MediaLibraryEntity(
            3,
            "磁链播放",
            "url://dandanplay_magnet_link",
            MediaType.MAGNET_LINK,
            null,
            null,
            true,
            0,
            "magnet:?xt=urn:btih:"
        )
        val historyEntity = MediaLibraryEntity(
            4,
            "播放历史",
            "",
            MediaType.OTHER_STORAGE
        )
        viewModelScope.launch(context = Dispatchers.IO) {
            //播放历史首条记录
            DatabaseManager.instance.getPlayHistoryDao().gitLastPlay(
                MediaType.LOCAL_STORAGE,
                MediaType.OTHER_STORAGE,
                MediaType.FTP_SERVER,
                MediaType.SMB_SERVER,
                MediaType.REMOTE_STORAGE,
                MediaType.WEBDAV_SERVER
            )?.apply {
                historyEntity.url = url
            }

            //磁链播放首条记录
            DatabaseManager.instance.getPlayHistoryDao().gitLastPlay(MediaType.MAGNET_LINK)?.apply {
                magnetEntity.describe = getFileName(torrentPath)
            }

            //串流播放首条记录
            DatabaseManager.instance.getPlayHistoryDao().gitLastPlay(MediaType.STREAM_LINK)?.apply {
                streamEntity.describe = url
            }

            DatabaseManager.instance.getMediaLibraryDao()
                .insert(localStorageEntity, streamEntity, magnetEntity, historyEntity)
        }
    }

    fun deleteStorage(data: MediaLibraryEntity) {
        viewModelScope.launch(context = Dispatchers.IO) {
            DatabaseManager.instance.getMediaLibraryDao()
                .delete(data.url, data.mediaType)
        }
    }

    fun checkScreenDeviceRunning(receiver: MediaLibraryEntity) {
        httpRequest<CommonJsonData>(viewModelScope) {

            api {
                Retrofit.screencastService.init(
                    host = receiver.screencastAddress,
                    port = receiver.port,
                    authorization = receiver.password,
                )
            }

            onStart { showLoading() }

            onSuccess {
                if (it.success) {
                    ToastCenter.showSuccess("投屏设备连接正常，请前往其它媒体库选择文件投屏")
                } else {
                    ToastCenter.showError(it.errorMessage ?: "连接至投屏设备失败，请确认投屏设备已启用接收服务")
                }
            }

            onError {
                ToastCenter.showError("连接至投屏设备失败，请确认投屏设备已启用接收服务")
            }

            onComplete { hideLoading() }
        }
    }
}