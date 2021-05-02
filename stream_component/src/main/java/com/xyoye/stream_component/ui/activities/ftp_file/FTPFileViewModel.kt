package com.xyoye.stream_component.ui.activities.ftp_file

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
import com.xyoye.stream_component.utils.PlayHistoryUtils
import com.xyoye.stream_component.utils.ftp.FTPException
import com.xyoye.stream_component.utils.ftp.FTPManager
import com.xyoye.stream_component.utils.server.FTPPlayServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile
import java.io.File

class FTPFileViewModel : BaseViewModel() {
    private val showHiddenFile = AppConfig.isShowHiddenFile()

    val fileLiveData = MutableLiveData<MutableList<FTPFile>>()
    val pathLiveData = MutableLiveData<MutableList<FilePathBean>>()
    val playVideoLiveData = MutableLiveData<PlayParams>()

    private var openedDirectoryList = mutableListOf<String>()

    /**
     * 初始化FTP，展开根目录
     */
    fun initFtp(serverData: MediaLibraryEntity) {
        showLoading()
        FTPManager.getInstance().initConfig(
                serverData.ftpAddress,
                serverData.port,
                serverData.account,
                serverData.password,
                serverData.ftpEncoding,
                serverData.isActiveFTP,
                serverData.isAnonymous
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                FTPManager.getInstance().connect()
                hideLoading()
            } catch (e: FTPException) {
                e.printStackTrace()
                hideLoading()
            }

            //打开根目录
            openChildDirectory("")
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
        showLoading()
        val currentDirPath = getOpenedDirPath()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val playServer = FTPPlayServer.getInstance()
                if (!playServer.isAlive) playServer.start()

                //获取播放Url，此时未设置播放资源
                val playUrl = playServer.getInputStreamUrl(fileName)
                //设置播放参数，弹幕、字幕等
                val playParams = buildPlayParams(playUrl, fileName)

                //获取文件流
                val inputStream = FTPManager.getInstance().getInputStream(currentDirPath, fileName)
                //传入播放资源到服务器
                playServer.setPlaySource(fileName, fileSize, inputStream)

                hideLoading()
                playVideoLiveData.postValue(playParams)
            } catch (e: FTPException) {
                e.printStackTrace()

                hideLoading()
                ToastCenter.showError("打开视频文件失败")
                return@launch
            }
        }
    }

    /**
     * 关闭FTP连接
     */
    fun closeFTP() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                FTPPlayServer.getInstance().closeIO()
                FTPPlayServer.getInstance().stop()
                FTPManager.getInstance().disconnect()
            } catch (e: FTPException) {
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
                val fileList = FTPManager.getInstance().listFiles(path)
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
            } catch (e: FTPException) {
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
            if (index != 0) {
                dirPath.append("/").append(name)
            }
        }
        return dirPath.toString()
    }

    private suspend fun buildPlayParams(playUrl: String, fileName: String): PlayParams {
        val playParams = PlayParams(
                playUrl,
                fileName.formatFileName(),
                null,
                null,
                0,
                0,
                MediaType.FTP_SERVER
        )

        val historyEntity = PlayHistoryUtils.getPlayHistory(playUrl, MediaType.FTP_SERVER)

        if (historyEntity?.danmuPath != null){
            //从播放记录读取弹幕
            playParams.danmuPath = historyEntity.danmuPath
            playParams.episodeId = historyEntity.episodeId
            DDLog.i("ftp danmu -----> database")
        } else if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
            //自动匹配同文件夹内同名弹幕
            playParams.danmuPath = findAndDownloadDanmu(fileName)
            DDLog.i("ftp danmu -----> download")
        }

        if (historyEntity?.subtitlePath != null){
            //从播放记录读取字幕
            playParams.subtitlePath = historyEntity.subtitlePath
            DDLog.i("ftp subtitle -----> database")
        } else if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()){
            //自动匹配同文件夹内同名字幕
            playParams.subtitlePath = findAndDownloadSubtitle(fileName)
            DDLog.i("ftp subtitle -----> download")
        }

        return playParams
    }

    private suspend fun findAndDownloadDanmu(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            //目标文件名
            val targetFileName = getFileNameNoExtension(fileName) + ".xml"
            val fileList = fileLiveData.value ?: return@withContext null

            val danmuFTPFile = fileList.find { it.name == targetFileName }
                    ?: return@withContext null

            val danmuFile = File(PathHelper.getDanmuDirectory(), fileName.formatFileName())

            val copySuccess = FTPManager.getInstance().copyFtpFile(getOpenedDirPath(), danmuFTPFile.name, danmuFile)
            if (copySuccess){
                return@withContext danmuFile.absolutePath
            } else {
                return@withContext null
            }
        }
    }

    private suspend fun findAndDownloadSubtitle(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            //视频文件名
            val videoFileName = getFileNameNoExtension(fileName) + "."
            val fileList = fileLiveData.value ?: return@withContext null

            val danmuFTPFile = fileList.find {
                SubtitleUtils.isSameNameSubtitle(it.name, videoFileName)
            } ?: return@withContext null

            val subtitleFile = File(PathHelper.getSubtitleDirectory(), danmuFTPFile.name.formatFileName())

            val copySuccess = FTPManager.getInstance().copyFtpFile(getOpenedDirPath(), danmuFTPFile.name, subtitleFile)
            if (copySuccess){
                return@withContext subtitleFile.absolutePath
            } else {
                return@withContext null
            }
        }
    }
}