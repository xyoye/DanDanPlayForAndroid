package com.xyoye.local_component.ui.activities.local_media

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.deduplication
import com.xyoye.common_component.extension.isInvalid
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestErrorHandler
import com.xyoye.common_component.resolver.MediaResolver
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class LocalMediaViewModel : BaseViewModel() {
    //当前是否在根目录
    val inRootFolder = ObservableBoolean()

    //当前是否为搜索状态
    val inSearchState = ObservableBoolean()

    //当前打开的目录名
    val currentFolderName = ObservableField<String>()

    //当前目录路径
    private val currentFolderPath = ObservableField<String>()

    val refreshLiveData = MutableLiveData<Boolean>()
    val refreshEnableLiveData = MutableLiveData<Boolean>()
    val folderLiveData = MutableLiveData<MutableList<FolderBean>>()
    val fileLiveData = MediatorLiveData<MutableList<VideoEntity>>()

    val playLiveData = MutableLiveData<Any>()

    private var searchJob: Job? = null

    //直接关联数据库的live data
    private var databaseVideoLiveData: LiveData<MutableList<VideoEntity>>? = null

    //记录最近一次本地播放的live data
    val lastPlayHistory = DatabaseManager.instance.getPlayHistoryDao()
        .gitLastPlayLiveData(MediaType.LOCAL_STORAGE)

    fun fastPlay() {
        viewModelScope.launch {
            val lastHistory = lastPlayHistory.value
            if (lastHistory == null) {
                ToastCenter.showError("无最近播放记录")
                return@launch
            }

            val (index, folderVideos) = getFolderVideos(lastHistory.url)
                ?: return@launch

            playIndexFromList(index, folderVideos)
        }
    }

    fun listRoot(deepRefresh: Boolean = false) {
        inRootFolder.set(true)
        inSearchState.set(false)

        if (deepRefresh.not() && folderLiveData.value != null) {
            backRoot(folderLiveData.value!!)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            refreshEnableLiveData.postValue(true)
            //深度刷新所有视频数据
            val refreshSuccess = refreshSystemVideo()
            if (refreshSuccess) {
                val folderData = DatabaseManager.instance.getVideoDao().getFolderByFilter()

                val lastPlayFolder = getLastPlayFolder()
                folderData.onEach {
                    it.isLastPlay = it.folderPath == lastPlayFolder
                }.sortWith(FileComparator(
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

    private fun backRoot(folderList: MutableList<FolderBean>) {
        val lastPlayFolder = getLastPlayFolder()
        folderList.forEach {
            it.isLastPlay = it.folderPath == lastPlayFolder
        }
        folderLiveData.postValue(folderList)
    }

    fun listFolder(folderName: String, folderPath: String) {
        inRootFolder.set(false)
        inSearchState.set(false)
        refreshEnableLiveData.postValue(false)
        currentFolderName.set(folderName)
        currentFolderPath.set(folderPath)

        viewModelScope.launch {
            databaseVideoLiveData?.let {
                fileLiveData.removeSource(it)
            }
            databaseVideoLiveData =
                DatabaseManager.instance.getVideoDao().getVideoInFolder(folderPath)
            updateFolderFileLiveData()
        }
    }

    fun updateLastPlay(filePath: String) {
        val folderPath = getDirPath(filePath)
        when {
            inSearchState.get() -> {
                fileLiveData.value?.onEach {
                    it.isLastPlay = it.filePath == filePath
                }?.let {
                    fileLiveData.postValue(it)
                }
            }
            inRootFolder.get() -> {
                folderLiveData.value?.onEach {
                    it.isLastPlay = it.folderPath == folderPath
                }?.let {
                    folderLiveData.postValue(it)
                }
            }
            currentFolderPath.get() == folderPath -> {
                fileLiveData.value?.onEach {
                    it.isLastPlay = it.filePath == filePath
                }?.let {
                    fileLiveData.postValue(it)
                }
            }
        }
    }

    fun exitSearchVideo() {
        inSearchState.set(false)
        if (inRootFolder.get()) {
            val lastPlayFolder = getLastPlayFolder()
            folderLiveData.value?.onEach {
                it.isLastPlay = it.folderPath == lastPlayFolder
            }?.let {
                folderLiveData.postValue(it)
            }
        } else {
            val folderName = currentFolderName.get()!!
            val folderPath = currentFolderPath.get()!!
            listFolder(folderName, folderPath)
        }
    }

    fun searchVideo(keyword: String) {
        inSearchState.set(true)
        refreshEnableLiveData.postValue(false)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            databaseVideoLiveData?.let {
                fileLiveData.removeSource(it)
            }

            val searchWord = "%$keyword%"
            databaseVideoLiveData = if (inRootFolder.get()) {
                DatabaseManager.instance.getVideoDao().searchVideo(searchWord)
            } else {
                DatabaseManager.instance.getVideoDao().searchVideoInFolder(
                    searchWord,
                    currentFolderPath.get()
                )
            }
            updateFolderFileLiveData(inRootFolder.get())
        }
    }

    fun matchDanmu(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            try {
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
            } catch (e: Exception) {
                val error = RequestErrorHandler(e).handlerError()
                showNetworkError(error)
            }
            hideLoading()
        }
    }

    fun playItem(itemPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (inSearchState.get()) {
                val video = fileLiveData.value?.get(itemPosition)
                if (video == null) {
                    ToastCenter.showError("播放失败，找不到播放资源")
                    return@launch
                }

                val (index, folderVideos) = getFolderVideos(video.filePath)
                    ?: return@launch

                playIndexFromList(index, folderVideos)
            } else {
                playIndexFromList(itemPosition, fileLiveData.value)
            }
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

    private fun updateFolderFileLiveData(updateInRootPath: Boolean = false) {
        databaseVideoLiveData ?: return

        fileLiveData.addSource(databaseVideoLiveData!!) { videoData ->
            if (updateInRootPath || inRootFolder.get().not()) {
                videoData.onEach {
                    it.isLastPlay = it.filePath == lastPlayHistory.value?.url
                }.sortWith(FileComparator(
                    value = { getFileName(it.filePath) },
                    isDirectory = { false }
                ))
                fileLiveData.postValue(videoData)
            }
        }
    }

    private suspend fun refreshSystemVideo(): Boolean {
        return withContext(Dispatchers.IO) {
            //1.从系统中读出所有视频数据
            val systemVideos = MediaResolver.queryVideo()

            //2.遍历扩展目录读出所有视频数据
            val extendFolderList = DatabaseManager.instance.getExtendFolderDao().getAll()
            extendFolderList.forEach {
                val extendVideos = MediaUtils.scanVideoFile(it.folderPath)

                //对扩展目录扫描出的视频去重
                extendVideos.iterator().deduplication(systemVideos) { extend, system ->
                    system.filePath == extend.filePath
                }

                //扩展目录视频数据也视为系统视频数据
                systemVideos.addAll(extendVideos)
            }

            //移除系统视频中无效的视频
            clearInvalidVideo(systemVideos)

            if (systemVideos.isEmpty())
                return@withContext false

            //3.从数据库中读出所有视频数据
            val databaseVideos = DatabaseManager.instance.getVideoDao().getAll()

            //4.数据库中无视频数据，直接将所有系统数据插入数据库
            if (databaseVideos.size == 0) {
                DatabaseManager.instance.getVideoDao().insert(*systemVideos.toTypedArray())
                return@withContext true
            }

            //5.遍历数据库数据
            for (databaseVideo in databaseVideos) {
                //数据库数据对应的视频是否已被删除
                var isDeleted = true
                //在系统数据中未找到数据库数据，说明视频已被删除
                val iterator = systemVideos.iterator()
                while (iterator.hasNext()) {
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

            return@withContext true
        }
    }

    private suspend fun checkSourceExist(videoEntity: VideoEntity) {
        if (videoEntity.danmuPath.toFile().isInvalid()) {
            DatabaseManager.instance.getVideoDao().updateDanmu(
                videoEntity.filePath, null, 0
            )
        }

        if (videoEntity.subtitlePath.toFile().isInvalid()) {
            DatabaseManager.instance.getVideoDao().updateSubtitle(
                videoEntity.filePath, null
            )
        }
    }

    /**
     * 清除已删除的文件
     */
    private fun clearInvalidVideo(entities: MutableList<VideoEntity>) {
        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            val filePath = iterator.next().filePath
            if (filePath.toFile().isInvalid()) {
                iterator.remove()
            }
        }
    }

    /**
     * 通过单个视频地址，获取其所在目录所有视频
     */
    private suspend fun getFolderVideos(filePath: String): Pair<Int, List<VideoEntity>>? {
        val folderVideos = DatabaseManager.instance.getVideoDao()
            .getFolderVideoByFilePath(filePath)
        folderVideos.sortWith(FileComparator(
            value = { getFileName(it.filePath) },
            isDirectory = { false }
        ))

        //如果视频地址对应的目录下找不到，可能视频已经被移除
        val index = folderVideos.indexOfFirst { it.filePath == filePath }
        if (index == -1) {
            ToastCenter.showError("播放失败，找不到播放资源")
            return null
        }
        return Pair(index, folderVideos)
    }

    /**
     * 播放视频列表中的某一个视频
     */
    private suspend fun playIndexFromList(index: Int, list: List<VideoEntity>?) {
        showLoading()
        val mediaSource = VideoSourceFactory.Builder()
            .setVideoSources(list ?: emptyList())
            .setIndex(index)
            .create(MediaType.LOCAL_STORAGE)
        hideLoading()

        if (mediaSource == null) {
            ToastCenter.showError("播放失败，找不到播放资源")
            return
        }
        VideoSourceManager.getInstance().setSource(mediaSource)
        playLiveData.postValue(Any())
    }

    private fun getLastPlayFolder(): String? {
        return lastPlayHistory.value?.url?.run {
            getDirPath(this)
        }
    }
}