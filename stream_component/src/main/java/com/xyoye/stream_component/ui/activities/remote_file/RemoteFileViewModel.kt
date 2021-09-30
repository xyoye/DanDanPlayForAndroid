package com.xyoye.stream_component.ui.activities.remote_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.utils.PlayHistoryUtils
import com.xyoye.stream_component.utils.remote.RemoteFileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemoteFileViewModel : BaseViewModel() {

    val playVideoLiveData = MutableLiveData<PlayParams>()
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

    fun openVideo(videoData: RemoteVideoData) {
        viewModelScope.launch {
            showLoading()
            val videoUrl = RemoteHelper.getInstance().buildVideoUrl(videoData.Id)
            val playParams = PlayParams(
                videoUrl,
                (videoData.EpisodeTitle ?: videoData.Name).formatFileName(),
                null,
                null,
                0,
                0,
                MediaType.REMOTE_STORAGE
            )

            val historyEntity = PlayHistoryUtils.getPlayHistory(videoUrl, MediaType.REMOTE_STORAGE)
            playParams.currentPosition = historyEntity?.videoPosition ?: 0

            if (historyEntity?.danmuPath != null) {
                //从播放记录读取弹幕
                playParams.danmuPath = historyEntity.danmuPath
                playParams.episodeId = historyEntity.episodeId
                DDLog.i("remote danmu -----> database")
            } else if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
                //自动匹配同文件夹内同名弹幕
                playParams.danmuPath = findAndDownloadDanmu(videoData)
                DDLog.i("remote danmu -----> download")
            }

            if (historyEntity?.subtitlePath != null) {
                //从播放记录读取字幕
                playParams.subtitlePath = historyEntity.subtitlePath
                DDLog.i("remote subtitle -----> database")
            } else if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()) {
                //自动匹配同文件夹内同名字幕
                playParams.subtitlePath = findAndDownloadSubtitle(videoData)
                DDLog.i("remote subtitle -----> download")
            }

            hideLoading()
            playVideoLiveData.postValue(playParams)
        }
    }

    private suspend fun findAndDownloadDanmu(videoData: RemoteVideoData): String? {
        return withContext(Dispatchers.IO) {

            try {
                val danmuResponseBody = Retrofit.remoteService.downloadDanmu(videoData.Hash)
                val videoName = videoData.EpisodeTitle ?: videoData.Name
                val danmuFileName = getFileNameNoExtension(videoName) + ".xml"
                return@withContext DanmuUtils.saveDanmu(
                    danmuFileName,
                    danmuResponseBody.byteStream()
                )
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            null
        }
    }

    private suspend fun findAndDownloadSubtitle(videoData: RemoteVideoData): String? {
        return withContext(Dispatchers.IO) {
            try {
                val subtitleData = Retrofit.remoteService.searchSubtitle(videoData.Id)
                if (subtitleData.subtitles.isNotEmpty()) {
                    val subtitleName = subtitleData.subtitles[0].fileName
                    val subtitleResponseBody =
                        Retrofit.remoteService.downloadSubtitle(videoData.Id, subtitleName)
                    return@withContext SubtitleUtils.saveSubtitle(
                        subtitleName,
                        subtitleResponseBody.byteStream()
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            null
        }
    }
}