package com.xyoye.stream_component.ui.activities.remote_file

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.utils.PlayHistoryUtils
import kotlinx.coroutines.launch

class RemoteFileViewModel : BaseViewModel() {
    //当前是否在根目录
    val inRootFolder = ObservableBoolean()

    //当前打开的目录名
    val currentFolderName = ObservableField<String>()

    val refreshLiveData = MutableLiveData<Boolean>()
    val folderLiveData = MutableLiveData<MutableList<FolderBean>>()
    val videoLiveData = MutableLiveData<MutableList<RemoteVideoData>>()
    val refreshEnableLiveData = MutableLiveData<Boolean>()
    val playVideoLiveData = MutableLiveData<PlayParams>()

    //远程媒体库所有文件数据
    private val storage = hashMapOf<String, MutableList<RemoteVideoData>>()

    fun openStorage(remoteData: MediaLibraryEntity) {
        val remoteUrl = "http://${remoteData.url}:${remoteData.port}/"
        val remoteToken = remoteData.remoteSecret
        RemoteHelper.getInstance().remoteUrl = remoteUrl
        RemoteHelper.getInstance().remoteToken = remoteToken

        httpRequest<Any>(viewModelScope) {

            onStart { refreshLiveData.postValue(true) }

            api {
                val videoData = Retrofit.remoteService.openStorage()
                storage.clear()
                videoData.forEach {
                    if (it.IsStandalone) {
                        val videoList = storage["其它"] ?: mutableListOf()
                        videoList.add(it)
                        storage["其它"] = videoList
                        return@forEach
                    }

                    val folderName = getParentFolderName(it.Path, "\\")
                    val videoList = storage[folderName] ?: mutableListOf()
                    videoList.add(it)
                    storage[folderName] = videoList
                }
            }

            onSuccess {
                listRoot()
            }

            onError {
                val errorMsg = if (it.code == 401) {
                    "连接失败：密钥验证失败"
                } else {
                    "连接失败：${it.message}"
                }

                ToastCenter.showWarning(errorMsg)
            }

            onComplete { refreshLiveData.postValue(false) }
        }
    }

    fun listRoot() {
        inRootFolder.set(true)
        refreshEnableLiveData.postValue(true)

        val folderList = mutableListOf<FolderBean>()

        var singleFileSize = 0

        storage.entries.forEach {
            if (it.key == "其它") {
                singleFileSize = it.value.size
            } else {
                folderList.add(
                    FolderBean(it.key, it.value.size)
                )
            }
        }

        //按文件名排序
        folderList.sortWith(FileComparator(
            value = { it.folderPath },
            isDirectory = { true }
        ))
        //独立文件放在最开头
        folderList.add(0, FolderBean("其它", singleFileSize))

        folderLiveData.postValue(folderList)
    }

    fun listFolder(folderName: String) {
        inRootFolder.set(false)
        refreshEnableLiveData.postValue(false)
        currentFolderName.set(folderName)

        //按文件名排序
        val videoList = storage[folderName] ?: mutableListOf()
        videoList.sortWith(FileComparator(
            value = { it.EpisodeTitle ?: it.Name },
            isDirectory = { false }
        ))
        videoLiveData.postValue(videoList)
    }

    fun openVideo(videoData: RemoteVideoData) {
        viewModelScope.launch {
            val videoUrl = RemoteHelper.getInstance().buildVideoUrl(videoData.Hash)
            val playParams = PlayParams(
                videoUrl,
                videoData.EpisodeTitle ?: videoData.Name,
                null,
                null,
                0,
                0,
                MediaType.REMOTE_STORAGE
            )

            val historyEntity = PlayHistoryUtils.getPlayHistory(videoUrl, MediaType.REMOTE_STORAGE)

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

            playVideoLiveData.postValue(playParams)
        }
    }

    private suspend fun findAndDownloadDanmu(videoData: RemoteVideoData): String? {
        return null
//        return withContext(Dispatchers.IO) {
//            //目标文件名
//            val targetFileName = getFileNameNoExtension(fileName) + ".xml"
//            val fileList = fileLiveData.value ?: return@withContext null
//
//            val danmuFTPFile = fileList.find { it.name == targetFileName }
//                ?: return@withContext null
//
//            val danmuFileName = fileName.trim().replace(" ", "_")
//            val danmuFile = File(PathHelper.getDanmuDirectory(), danmuFileName)
//
//            val copySuccess = FTPManager.getInstance().copyFtpFile(getOpenedDirPath(), danmuFTPFile.name, danmuFile)
//            if (copySuccess){
//                return@withContext danmuFile.absolutePath
//            } else {
//                return@withContext null
//            }
//        }
    }

    private suspend fun findAndDownloadSubtitle(videoData: RemoteVideoData): String? {
        return null
//        return withContext(Dispatchers.IO) {
//            //视频文件名
//            val videoFileName = getFileNameNoExtension(fileName) + "."
//            val fileList = fileLiveData.value ?: return@withContext null
//
//            val danmuFTPFile = fileList.find {
//                SubtitleUtils.isSameNameSubtitle(it.name, videoFileName)
//            } ?: return@withContext null
//
//            val subtitleFileName = danmuFTPFile.name.trim().replace(" ", "_")
//            val subtitleFile = File(PathHelper.getSubtitleDirectory(), subtitleFileName)
//
//            val copySuccess = FTPManager.getInstance().copyFtpFile(getOpenedDirPath(), danmuFTPFile.name, subtitleFile)
//            if (copySuccess){
//                return@withContext subtitleFile.absolutePath
//            } else {
//                return@withContext null
//            }
//        }
    }
}