package com.xyoye.stream_component.ui.activities.web_dav_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.helper.UnsafeOkHttpClient
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.factory.WebDavSourceFactory
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.bean.WebDavFileBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.sardine.DavResource
import com.xyoye.sardine.impl.OkHttpSardine
import com.xyoye.sardine.util.SardineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import java.net.URI

class WebDavFileViewModel : BaseViewModel() {
    companion object {
        private const val PATH_ROOT = "/"
    }

    private lateinit var addressUrl: String
    private lateinit var credentials: String
    private var rootPath = PATH_ROOT

    private val curDirectoryFiles = mutableListOf<DavResource>()

    val fileLiveData = MutableLiveData<List<Any>>()
    val pathLiveData = MutableLiveData<List<FilePathBean>>()
    val playLiveData = MutableLiveData<Any>()

    private lateinit var sardine: OkHttpSardine

    fun listStorageRoot(serverData: MediaLibraryEntity) {
        SardineConfig.isXmlStrictMode = serverData.webDavStrict

        sardine = OkHttpSardine(UnsafeOkHttpClient.client)
        if (!serverData.account.isNullOrEmpty()) {
            val account = serverData.account!!
            val password = serverData.password!!
            credentials = Credentials.basic(account, password)
            sardine.setCredentials(account, password)
        }

        if (parseAddress(serverData.url)) {
            openDirectory(rootPath)
        } else {
            ToastCenter.showError("解析链接失败：${serverData.url}")
        }
    }

    fun openDirectory(path: String) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val childFiles = sardine.list(addressUrl + path)
                    .filter {
                        val isNotCurrentDir = formatPath(it.path) != formatPath(path)
                        val isNotNeedHide = FileNameUtils.fileNeedHide(it.name).not()
                        //过滤同路径文件夹 和 .开头的隐藏文件
                        isNotCurrentDir && isNotNeedHide
                    }
                curDirectoryFiles.clear()
                curDirectoryFiles.addAll(childFiles)

                refreshDirectory()
                //获取路径列表
                pathLiveData.postValue(splitPath(path))
            } catch (t: Throwable) {
                t.printStackTrace()
                ToastCenter.showError("连接失败：${t.message}")
            }
            withContext(Dispatchers.Main) {
                hideLoading()
            }
        }
    }

    fun refreshDirectory() {
        viewModelScope.launch(Dispatchers.IO) {
            val displayFiles = curDirectoryFiles
                .filter { it.isDirectory || isVideoFile(it.name) }
                .sortedWith(FileComparator(
                    value = { it.name },
                    isDirectory = { it.isDirectory }
                ))
                .map {
                    if (it.isDirectory) {
                        return@map it
                    }
                    val uniqueKey = WebDavSourceFactory.generateUniqueKey(addressUrl, it)
                    val history = DatabaseManager.instance
                        .getPlayHistoryDao()
                        .getHistoryByKey(uniqueKey, MediaType.WEBDAV_SERVER)
                    WebDavFileBean(
                        it.href,
                        it.name ?: it.displayName,
                        history?.danmuPath,
                        history?.subtitlePath,
                        history?.videoPosition ?: 0,
                        history?.videoDuration ?: 0,
                        uniqueKey
                    )
                }

            fileLiveData.postValue(displayFiles)
        }
    }

    fun openParentDirectory(): Boolean {
        val pathList = pathLiveData.value ?: return false
        //当前目录已在根目录，退出
        if (pathList.size < 2)
            return false
        val targetPath = pathList[pathList.size - 2].path
        //目标是初次打开目录的父目录，退出
        if (targetPath != rootPath && rootPath.startsWith(targetPath))
            return false
        openDirectory(targetPath)
        return true
    }

    fun playItem(href: URI) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoSources = curDirectoryFiles.filter { isVideoFile(it.name) }
            val index = videoSources.indexOfFirst { it.href == href }
            if (videoSources.isNullOrEmpty() || index < 0) {
                ToastCenter.showError("播放失败，不支持播放的资源")
                return@launch
            }

            //同文件夹内的弹幕和字幕资源
            val extSources = curDirectoryFiles
                .filter { isDanmuFile(it.name) || isSubtitleFile(it.name) }
            //身份验证请求头
            val header = mapOf(Pair("Authorization", credentials))

            showLoading()
            val mediaSource = VideoSourceFactory.Builder()
                .setVideoSources(videoSources)
                .setExtraSource(extSources)
                .setRootPath(addressUrl)
                .setHttpHeaders(header)
                .setIndex(index)
                .create(MediaType.WEBDAV_SERVER)
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }
            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }

    private fun parseAddress(serverUrl: String): Boolean {
        val schemeStart = serverUrl.indexOf("//")
        if (schemeStart < 0) {
            return false
        }
        val domainEnd = serverUrl.indexOf("/", schemeStart + 2)
        if (domainEnd < 0) {
            return false
        }

        addressUrl = serverUrl.substring(0, domainEnd)
        rootPath = formatPath(serverUrl.substring(domainEnd))

        return true
    }

    private fun splitPath(path: String): MutableList<FilePathBean> {
        return if (PATH_ROOT == path) {
            mutableListOf(FilePathBean("根目录", PATH_ROOT, true))
        } else {
            //去掉前后反斜杠
            var pathText = path
            if (pathText.startsWith("/")) {
                pathText = pathText.substring(1, pathText.length)
            }
            if (pathText.endsWith("/")) {
                pathText = pathText.substring(0, pathText.length - 1)
            }
            //添加根目录
            val pathList = mutableListOf(FilePathBean("根目录", PATH_ROOT, false))
            //分解当前打开目录
            val pathNameList = pathText.split("/").toMutableList()
            if (pathNameList.size == 0) {
                pathList.add(FilePathBean(pathText, "/$pathText", false))
            } else {
                var currentPath = ""
                pathNameList.forEach {
                    currentPath += "/$it"
                    //转换为目录数据
                    pathList.add(FilePathBean(it, currentPath, false))
                }
            }
            //设置最后一个文件夹为打开状态
            pathList.last().isOpened = true
            pathList
        }
    }

    private fun formatPath(path: String): String {
        return when {
            path == "/" -> "/"
            path.endsWith("/") -> path.substring(0, path.length - 1)
            else -> path
        }
    }
}