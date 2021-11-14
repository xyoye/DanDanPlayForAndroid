package com.xyoye.stream_component.ui.activities.smb_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.utils.FileHashUtils
import com.xyoye.stream_component.utils.PlayHistoryUtils
import com.xyoye.stream_component.utils.server.SMBPlayServer
import com.xyoye.stream_component.utils.smb.SMBException
import com.xyoye.stream_component.utils.smb.SMBFile
import com.xyoye.stream_component.utils.smb.v2.SMBJManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmbFileViewModel : BaseViewModel() {
    private val showHiddenFile = AppConfig.isShowHiddenFile()

    val fileLiveData = MutableLiveData<MutableList<SMBFile>>()
    val pathLiveData = MutableLiveData<MutableList<FilePathBean>>()
    val playVideoLiveData = MutableLiveData<PlayParams>()

    private var openedDirectoryList = mutableListOf<String>()

    /**
     * 初始化SMB，展开根目录
     */
    fun initFtp(serverData: MediaLibraryEntity) {
        showLoading()
        SMBJManager.getInstance().initConfig(
            serverData.url,
            serverData.account,
            serverData.password,
            serverData.isAnonymous
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SMBJManager.getInstance().connect()
                hideLoading()
            } catch (e: SMBException) {
                e.printStackTrace()
                hideLoading()
            }

            val rootPath = serverData.smbSharePath ?: ""

            //打开根目录
            openChildDirectory(rootPath)
        }
    }

    /**
     * 打开子目录
     */
    fun openChildDirectory(dirName: String) {
        //新增当前目录
        openedDirectoryList.add(dirName)
        //合并为目录地址
        val dirPath = getOpenedDirPath()
        updatePath()
        //获取目录文件
        showLoading()
        listDirectory(dirPath)
    }

    /**
     * 打开第position层已展开的目录
     */
    fun openPositionDirectory(position: Int) {
        //仅支持打开当前已打开目录的父目录
        if (position >= openedDirectoryList.size)
            return

        //获取目标位置目录列表，目标位置 == +1
        openedDirectoryList = openedDirectoryList.subList(0, position + 1)
        //合并为目录地址
        val dirPath = getOpenedDirPath()
        updatePath()

        //获取目录文件
        showLoading()
        listDirectory(dirPath)
    }

    /**
     * 刷新当前目录
     */
    fun refreshDirectory() {
        //合并为目录地址
        val dirPath = getOpenedDirPath()

        //获取目录文件
        listDirectory(dirPath)
    }

    /**
     * 打开父目录
     */
    fun openParentDirectory(): Boolean {
        //当前已在根目录，无法打开父目录
        if (openedDirectoryList.size <= 1)
            return false

        //移除当前目录
        openedDirectoryList.removeLast()

        //合并为目录地址
        val dirPath = getOpenedDirPath()
        updatePath()

        //获取目录文件
        showLoading()
        listDirectory(dirPath)
        return true
    }

    /**
     * 打开视频文件
     */
    fun openVideoFile(fileName: String, fileSize: Long) {
        //仅支持视频文件
        if (!isVideoFile(fileName)) {
            ToastCenter.showWarning("不支持的视频文件格式")
            return
        }

        showLoading()
        val currentDirPath = getOpenedDirPath()
        val filePath = "$currentDirPath\\$fileName"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                //获取视频文件流前，关闭之前的文件流
                SMBJManager.getInstance().closeStream()
                //启动本地服务处理InputStream
                val playServer = SMBPlayServer.getInstance()
                if (!playServer.isAlive) playServer.start()

                val playUrl = playServer.getInputStreamUrl(
                    fileName, filePath, fileSize
                ) {
                    //获取文件流
                    SMBJManager.getInstance().getInputStream(it)
                }

                val playParams = buildPlayParams(playUrl, filePath, fileName)
                hideLoading()
                playVideoLiveData.postValue(playParams)
            } catch (e: SMBException) {
                e.printStackTrace()

                hideLoading()
                ToastCenter.showError("打开视频文件失败")
                return@launch
            }
        }
    }

    /**
     * 关闭播放文件流
     */
    fun closeStream() {
        viewModelScope.launch(Dispatchers.IO) {
            SMBPlayServer.getInstance().stop()
            SMBJManager.getInstance().closeStream()
        }
    }

    /**
     * 关闭SMB连接
     */
    fun closeSMB() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                SMBPlayServer.getInstance().closeIO()
                SMBJManager.getInstance().disConnect()
            } catch (e: SMBException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取文件夹内所有文件
     */
    private fun listDirectory(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileList = SMBJManager.getInstance().listFiles(path)
                fileList.sortWith(FileComparator(
                    value = { it.name },
                    isDirectory = { it.isDirectory }
                ))
                if (showHiddenFile) {
                    fileLiveData.postValue(fileList)
                } else {
                    fileLiveData.postValue(fileList.filter {
                        //过滤以.开头的文件
                        !it.name.startsWith(".")
                    }.toMutableList())
                }
                hideLoading()
            } catch (e: SMBException) {
                fileLiveData.postValue(mutableListOf())
                e.printStackTrace()
                hideLoading()
                ToastCenter.showError("获取文件列表失败：${e.message}")
                return@launch
            }
        }
    }

    /**
     * 更新界面上的目录列表
     */
    private fun updatePath() {
        val pathList = mutableListOf<FilePathBean>()
        openedDirectoryList.forEach {
            pathList.add(FilePathBean(it, "", false))
        }
        pathList.first().name = "根目录"
        pathList.last().isOpened = true
        pathLiveData.postValue(pathList)
    }

    /**
     * 获取当前展开目录的路径
     */
    private fun getOpenedDirPath(): String {
        //合并为目录地址
        val dirPath = StringBuilder()
        for ((index, name) in openedDirectoryList.withIndex()) {
            if (index == 0 && name == "") {
                continue
            }
            dirPath.append("\\").append(name)
        }
        return dirPath.toString()
    }

    private suspend fun buildPlayParams(
        playUrl: String,
        filePath: String,
        fileName: String
    ): PlayParams {
        val playParams = PlayParams(
            playUrl,
            fileName.formatFileName(),
            null,
            null,
            0,
            0,
            MediaType.SMB_SERVER
        )

        val historyEntity = PlayHistoryUtils.getPlayHistory(playUrl, MediaType.SMB_SERVER)
        playParams.currentPosition = historyEntity?.videoPosition ?: 0

        if (historyEntity?.danmuPath != null){
            //从播放记录读取弹幕
            playParams.danmuPath = historyEntity.danmuPath
            playParams.episodeId = historyEntity.episodeId
            DDLog.i("smb danmu -----> database")
        } else if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
            //自动匹配同文件夹内同名弹幕
            playParams.danmuPath = findAndDownloadDanmu(fileName)
            DDLog.i("smb danmu -----> download")
        }

        if (historyEntity?.subtitlePath != null){
            //从播放记录读取字幕
            playParams.subtitlePath = historyEntity.subtitlePath
            DDLog.i("smb subtitle -----> database")
        } else if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()){
            //自动匹配同文件夹内同名字幕
            playParams.subtitlePath = findAndDownloadSubtitle(fileName)
            DDLog.i("smb subtitle -----> download")
        }

        //是否自动匹配视频网络弹幕
        val autoMatchDanmuNetworkStorage = DanmuConfig.isAutoMatchDanmuNetworkStorage()
        if (playParams.danmuPath.isNullOrEmpty() && autoMatchDanmuNetworkStorage) {
            //获取视频文件hash
            val stream = SMBJManager.getInstance().getInputStream(filePath)
            val fileHash = FileHashUtils.getHash(stream)
            if (!fileHash.isNullOrEmpty()) {
                //根据hash匹配弹幕
                DanmuUtils.matchDanmuSilence(filePath, fileHash)?.let {
                    playParams.danmuPath = it.first
                    playParams.episodeId = it.second
                    DDLog.i("smb danmu -----> match download")
                }
            }
        }

        return playParams
    }

    private suspend fun findAndDownloadDanmu(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            //目标文件名
            val targetFileName = getFileNameNoExtension(fileName) + ".xml"
            val fileList = fileLiveData.value ?: return@withContext null

            val danmuSmbFile = fileList.find { it.name == targetFileName }
                ?: return@withContext null

            val danmuInputStream = SMBJManager.getInstance().getInputStream(
                "${getOpenedDirPath()}\\${danmuSmbFile.name}"
            )
            return@withContext DanmuUtils.saveDanmu(danmuSmbFile.name, danmuInputStream)
        }
    }

    private suspend fun findAndDownloadSubtitle(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            //视频文件名
            val videoFileName = getFileNameNoExtension(fileName) + "."
            val fileList = fileLiveData.value ?: return@withContext null

            val danmuSmbFile = fileList.find {
                SubtitleUtils.isSameNameSubtitle(it.name, videoFileName)
            } ?: return@withContext null

            val subtitleInputStream = SMBJManager.getInstance().getInputStream(
                "${getOpenedDirPath()}\\${danmuSmbFile.name}"
            )
            return@withContext SubtitleUtils.saveSubtitle(danmuSmbFile.name, subtitleInputStream)
        }
    }
}