package com.xyoye.local_component.ui.activities.local_media

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.deduplication
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestErrorHandler
import com.xyoye.common_component.resolver.MediaResolver
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.data.SubtitleThunderData
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.utils.SubtitleHashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class LocalMediaViewModel : BaseViewModel() {
    val inRootFolder = ObservableBoolean()
    val currentFolderName = ObservableField<String>()
    private val currentFolderPath = ObservableField<String>()

    val refreshLiveData = MutableLiveData<Boolean>()
    val refreshEnableLiveData = MutableLiveData<Boolean>()
    val folderLiveData = MutableLiveData<MutableList<FolderBean>>()
    val fileLiveData = MediatorLiveData<MutableList<VideoEntity>>()

    val playVideoLiveData = MutableLiveData<PlayParams>()

    private var folderFileLiveData: LiveData<MutableList<VideoEntity>>? = null

    val lastPlayLiveData =
        DatabaseManager.instance.getPlayHistoryDao().gitLastPlayLiveData(MediaType.LOCAL_STORAGE)

    fun fastPlay() {
        //播放最后一次播放的视频
        lastPlayLiveData.value?.let { entity ->
            playVideoLiveData.postValue(
                PlayParams(
                    entity.url,
                    entity.videoName,
                    entity.danmuPath,
                    entity.subtitlePath,
                    entity.videoPosition,
                    entity.episodeId,
                    entity.mediaType
                )
            )
        }
    }

    fun listRoot() {
        inRootFolder.set(true)
        refreshEnableLiveData.postValue(true)

        viewModelScope.launch {
            val refreshSuccess = refreshSystemVideo()
            if (refreshSuccess) {
                val folderData = DatabaseManager.instance.getVideoDao().getFolderByFilter()

                //是否为最后一次播放的文件所在文件夹
                lastPlayLiveData.value?.apply {
                    val folderPath = getDirPath(url)
                    folderData.find { it.folderPath == folderPath }?.isLastPlay = true
                }

                folderData.sortWith(FileComparator(
                    value = { getFolderName(it.folderPath) },
                    isDirectory = { true }
                ))
                folderLiveData.postValue(folderData)
                refreshLiveData.postValue(true)
            } else {
                refreshLiveData.postValue(false)
            }
        }
    }

    fun listFolder(folderName: String, folderPath: String) {
        inRootFolder.set(false)
        refreshEnableLiveData.postValue(false)
        currentFolderName.set(folderName)
        currentFolderPath.set(folderPath)

        viewModelScope.launch {
            folderFileLiveData?.let {
                fileLiveData.removeSource(it)
            }
            folderFileLiveData = DatabaseManager.instance.getVideoDao().getVideoInFolder(folderPath)
            fileLiveData.addSource(folderFileLiveData!!) { videoData ->
                if (!inRootFolder.get()) {

                    //是否为最后一次播放的文件
                    lastPlayLiveData.value?.apply {
                        videoData.find { it.filePath == url }?.isLastPlay = true
                    }

                    videoData.sortWith(FileComparator(
                        value = { getFileName(it.filePath) },
                        isDirectory = { false }
                    ))
                    fileLiveData.postValue(videoData)
                }
            }
        }
    }

    fun matchDanmu(filePath: String) {
        viewModelScope.launch {
            showLoading()
            try {
                withContext(Dispatchers.IO) {

                    //提取视频信息
                    val params = HashMap<String, String>()

                    params["fileName"] = getFileName(filePath)
                    params["fileHash"] = IOUtils.getFileHash(filePath) ?: ""
                    params["fileSize"] = File(filePath).length().toString()
                    params["videoDuration"] = "0"
                    params["matchMode"] = "hashOnly"

                    //匹配弹幕
                    val danmuMatchData = Retrofit.service.matchDanmu(params)

                    //只存在一个匹配的弹幕
                    if (danmuMatchData.isMatched && danmuMatchData.matches!!.size == 1) {
                        val episodeId = danmuMatchData.matches!![0].episodeId
                        val danmuData = Retrofit.service.getDanmuContent(episodeId.toString(), true)

                        val folderName = getParentFolderName(filePath)
                        val fileNameNotExt = getFileNameNoExtension(filePath)
                        //保存弹幕
                        val danmuPath =
                            DanmuUtils.saveDanmu(danmuData, folderName, "$fileNameNotExt.xml")

                        if (danmuPath.isNullOrEmpty()) {
                            ToastCenter.showError("保存弹幕失败")
                        } else {
                            //刷新数据库
                            DatabaseManager.instance
                                .getVideoDao()
                                .updateDanmu(filePath, danmuPath, episodeId)
                            ToastCenter.showSuccess("匹配弹幕成功！")
                        }
                    } else {
                        ToastCenter.showError("未匹配到相关弹幕")
                    }
                }
            } catch (e: Exception) {
                val error = RequestErrorHandler(e).handlerError()
                showNetworkError(error)
            }
            hideLoading()
        }
    }

    fun checkPlayParams(data: VideoEntity) {
        viewModelScope.launch {
            val playParams = PlayParams(
                data.filePath,
                getFileName(data.filePath),
                data.danmuPath,
                data.subtitlePath,
                0L,
                data.danmuId,
                MediaType.LOCAL_STORAGE
            )

            //读取上次播放位置
            val playHistory = DatabaseManager.instance.getPlayHistoryDao()
                .getPlayHistory(data.filePath, MediaType.LOCAL_STORAGE)
            if (playHistory.size > 0) {
                playParams.currentPosition = playHistory.first().videoPosition
            }

            //更新最后一次播放的Item
            val updateFileData = fileLiveData.value?.onEach {
                it.isLastPlay = it.filePath == data.filePath
            }
            fileLiveData.postValue(updateFileData)

            //已存在弹幕及字幕
            if (data.danmuPath != null && data.subtitlePath != null) {
                playVideoLiveData.postValue(playParams)
                return@launch
            }

            //自动加载弹幕及字幕
            autoLoadSource(data, playParams)
        }
    }

    fun removeDanmu(filePath: String) {
        viewModelScope.launch {
            DatabaseManager.instance.getVideoDao()
                .updateDanmu(filePath, null, 0)
        }
    }

    fun removeSubtitle(filePath: String) {
        viewModelScope.launch {
            DatabaseManager.instance.getVideoDao()
                .updateSubtitle(filePath, null)
        }
    }

    private suspend fun refreshSystemVideo(): Boolean {
        return viewModelScope.async (Dispatchers.IO) {
            //1.从系统中读出所有视频数据
            val systemVideos = MediaResolver.queryVideo()

            //2.遍历扩展目录读出所有视频数据
            val extendFolderList = DatabaseManager.instance.getExtendFolderDao().getAll()
            extendFolderList.forEach {
                val extendVideos = MediaUtils.scanVideoFile(it.folderPath)

                //对扩展目录扫描出的视频去重
                extendVideos.iterator().deduplication(systemVideos){ extend, system ->
                    system.filePath == extend.filePath
                }

                //扩展目录视频数据也视为系统视频数据
                systemVideos.addAll(extendVideos)
            }

            if (systemVideos.isEmpty())
                return@async false

            //3.从数据库中读出所有视频数据
            val databaseVideos = DatabaseManager.instance.getVideoDao().getAll()

            //4.数据库中无视频数据，直接将所有系统数据插入数据库
            if (databaseVideos.size == 0) {
                DatabaseManager.instance.getVideoDao().insert(*systemVideos.toTypedArray())
                return@async true
            }

            //5.遍历数据库数据
            for (databaseVideo in databaseVideos) {
                //数据库数据对应的视频是否已被删除
                var isDeleted = true
                //在系统数据中未找到数据库数据，说明视频已被删除
                val iterator = systemVideos.iterator()
                while (iterator.hasNext()){
                    if (iterator.next().filePath == databaseVideo.filePath) {
                        //数据库文件能在系统数据中找到，未被删除
                        isDeleted = false
                        //未删除的视频，需要检查弹幕及字幕文件
                        checkSourceExist(databaseVideo)
                        //未删除的视频，不需要删除数据库数据
                        iterator.remove()
                    }
                }

                //已删除的视频，从数据库删除
                if (isDeleted) {
                    DatabaseManager.instance.getVideoDao()
                        .deleteByPath(databaseVideo.filePath)
                }
            }

            //6.将剩余不在数据库的数据，插入数据库
            if (systemVideos.isNotEmpty()) {
                DatabaseManager.instance.getVideoDao().insert(*systemVideos.toTypedArray())
            }

            return@async true
        }.await()
    }

    private suspend fun autoLoadSource(data: VideoEntity, playParams: PlayParams) {
        withContext(Dispatchers.Default) {
            showLoading()
            //未绑定弹幕，尝试自动加载
            if (data.danmuPath == null) {
                //自动加载本地同名弹幕
                var loadedDanmu = false
                val autoLoadLocalDanmu = DanmuConfig.isAutoLoadLocalDanmu()
                if (autoLoadLocalDanmu) {
                    //从本地找同名弹幕
                    DanmuUtils.findLocalDanmuByVideo(data.filePath)?.let {
                        playParams.danmuPath = it
                        loadedDanmu = true
                    }
                }
                //自动加载网络弹幕
                val autoLoadNetworkDanmu = DanmuConfig.isAutoLoadNetworkDanmu()
                if (!loadedDanmu && autoLoadNetworkDanmu) {
                    val fileHash = IOUtils.getFileHash(data.filePath)
                    if (!fileHash.isNullOrEmpty()){
                        DanmuUtils.matchDanmuSilence(viewModelScope, data.filePath, fileHash)?.let {
                            playParams.danmuPath = it.first
                            playParams.episodeId = it.second
                            loadedDanmu = true
                        }
                    }
                }

                if (loadedDanmu) {
                    DatabaseManager.instance.getVideoDao().updateDanmu(
                        data.filePath, playParams.danmuPath!!, playParams.episodeId
                    )
                }
            }

            if (data.subtitlePath == null) {
                //自动加载本地同名字幕
                var loadedSubtitle = false
                val autoLoadLocalSubtitle = SubtitleConfig.isAutoLoadLocalSubtitle()
                if (autoLoadLocalSubtitle) {
                    //从本地找同名字幕
                    SubtitleUtils.findLocalSubtitleByVideo(data.filePath)?.let {
                        playParams.subtitlePath = it
                        loadedSubtitle = true
                    }
                }
                //自动加载网络字幕
                val autoLoadNetworkSubtitle = SubtitleConfig.isAutoLoadNetworkSubtitle()
                if (!loadedSubtitle && autoLoadNetworkSubtitle) {
                    matchSubtitleSilence(data.filePath)?.let {
                        playParams.subtitlePath = it
                        loadedSubtitle = true
                    }
                }

                if (loadedSubtitle) {
                    DatabaseManager.instance.getVideoDao().updateSubtitle(
                        data.filePath, playParams.subtitlePath!!
                    )
                }
            }

            hideLoading()
            playVideoLiveData.postValue(playParams)
        }
    }

    private suspend fun matchSubtitleSilence(filePath: String): String? {
        return viewModelScope.async(Dispatchers.IO) {
            val videoHash = SubtitleHashUtils.getThunderHash(filePath)
            if (videoHash != null) {
                //从迅雷匹配字幕
                val thunderUrl = "http://sub.xmp.sandai.net:8000/subxl/$videoHash.json"
                var subtitleData: SubtitleThunderData? = null
                try {
                    subtitleData = Retrofit.extService.matchThunderSubtitle(thunderUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //字幕内容存在
                subtitleData?.sublist?.let {
                    val subtitleName = it[0].sname
                    val subtitleUrl = it[0].surl
                    if (subtitleName != null && subtitleUrl != null) {
                        try {
                            //下载保存字幕
                            val responseBody = Retrofit.extService.downloadResource(subtitleUrl)
                            return@async SubtitleUtils.saveSubtitle(
                                subtitleName,
                                responseBody.byteStream()
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            null
        }.await()
    }

    private suspend fun checkSourceExist(videoEntity: VideoEntity){
        if (!videoEntity.danmuPath.isNullOrEmpty()){
            if (!isFileExist(videoEntity.danmuPath)){
                DatabaseManager.instance.getVideoDao().updateDanmu(
                    videoEntity.filePath, null, 0
                )
            }
        }

        if (!videoEntity.subtitlePath.isNullOrEmpty()){
            if (!isFileExist(videoEntity.subtitlePath)){
                DatabaseManager.instance.getVideoDao().updateSubtitle(
                    videoEntity.filePath, null
                )
            }
        }
    }
}