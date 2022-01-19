package com.xyoye.stream_component.ui.activities.ftp_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.filterHiddenFile
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.ftp.FTPException
import com.xyoye.common_component.utils.ftp.FTPManager
import com.xyoye.common_component.utils.server.FTPPlayServer
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.net.ftp.FTPFile

class FTPFileViewModel : BaseViewModel() {

    val fileLiveData = MutableLiveData<List<FTPFile>>()
    val pathLiveData = MutableLiveData<MutableList<FilePathBean>>()
    val playLiveData = MutableLiveData<Any>()

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
    fun openVideoFile(ftpFile: FTPFile) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val playServer = FTPPlayServer.getInstance()
                if (!playServer.isAlive) playServer.start()
            } catch (e: Exception) {
                ToastCenter.showError("启动FTP播放服务失败，请重试\n${e.message}")
                return@launch
            }

            val videoSources = fileLiveData.value
                ?.filter { isVideoFile(it.name) }
            val index = videoSources?.indexOf(ftpFile)
                ?: -1
            if (videoSources.isNullOrEmpty() || index < 0) {
                ToastCenter.showError("播放失败，不支持播放的资源")
                return@launch
            }

            //同文件夹内的弹幕和字幕资源
            val extSources = fileLiveData.value
                ?.filter { isDanmuFile(it.name) || isSubtitleFile(it.name) }
                ?: emptyList()

            showLoading()
            val mediaSource = VideoSourceFactory.Builder()
                .setVideoSources(videoSources)
                .setExtraSource(extSources)
                .setRootPath(getOpenedDirPath())
                .setIndex(index)
                .create(MediaType.FTP_SERVER)
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }
            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
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

                fileLiveData.postValue(fileList.filterHiddenFile { it.name })
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
}