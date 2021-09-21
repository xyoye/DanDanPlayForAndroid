package com.xyoye.stream_component.ui.activities.web_dav_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.helper.UnsafeOkHttpClient
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.sardine.DavResource
import com.xyoye.sardine.impl.OkHttpSardine
import com.xyoye.sardine.util.SardineConfig
import com.xyoye.stream_component.utils.FileHashUtils
import com.xyoye.stream_component.utils.PlayHistoryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials

class WebDavFileViewModel : BaseViewModel() {
    companion object {
        private const val PATH_ROOT = "/"
    }

    private val showHiddenFile = AppConfig.isShowHiddenFile()
    private lateinit var addressUrl: String
    private lateinit var credentials: String
    private var rootPath = PATH_ROOT

    val fileLiveData = MutableLiveData<MutableList<DavResource>>()
    val pathLiveData = MutableLiveData<MutableList<FilePathBean>>()
    val openVideoLiveData = MutableLiveData<PlayParams>()

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
                //获取文件列表，过滤同路径文件夹
                val fileList = sardine.list(addressUrl + path)
                    .filter { formatPath(it.path) != formatPath(path) }
                    .toMutableList()

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

    fun buildPlayParams(davResource: DavResource) {
        val url = addressUrl + davResource.href.toASCIIString()
        val header = mapOf(Pair("Authorization", credentials))

        val playParams = PlayParams(
            url,
            getFileName(davResource.name).formatFileName(),
            null,
            null,
            0,
            0,
            MediaType.WEBDAV_SERVER
        ).apply {
            setHttpHeader(JsonHelper.toJson(header))
        }

        showLoading()
        viewModelScope.launch {
            //从播放历史查看是否已绑定弹幕
            val historyEntity = PlayHistoryUtils.getPlayHistory(url, MediaType.WEBDAV_SERVER)

            if (historyEntity?.danmuPath != null) {
                //从播放记录读取弹幕
                playParams.danmuPath = historyEntity.danmuPath
                playParams.episodeId = historyEntity.episodeId
                DDLog.i("ftp danmu -----> database")
            } else if (DanmuConfig.isAutoLoadDanmuNetworkStorage()) {
                //自动匹配同文件夹内同名弹幕
                playParams.danmuPath = findAndDownloadDanmu(davResource, header)
                DDLog.i("ftp danmu -----> download")
            }

            if (historyEntity?.subtitlePath != null) {
                //从播放记录读取字幕
                playParams.subtitlePath = historyEntity.subtitlePath
                DDLog.i("ftp subtitle -----> database")
            } else if (SubtitleConfig.isAutoLoadSubtitleNetworkStorage()) {
                //自动匹配同文件夹内同名字幕
                playParams.subtitlePath = findAndDownloadSubtitle(davResource, header)
                DDLog.i("ftp subtitle -----> download")
            }

            //是否自动匹配视频网络弹幕
            val autoMatchDanmuNetworkStorage = DanmuConfig.isAutoMatchDanmuNetworkStorage()
            if (playParams.danmuPath.isNullOrEmpty() && autoMatchDanmuNetworkStorage) {
                //根据hash匹配弹幕
                matchAndDownloadDanmu(davResource, header)?.let {
                    playParams.danmuPath = it.first
                    playParams.episodeId = it.second
                    DDLog.i("dav danmu -----> match download")
                }
            }

            hideLoading()
            openVideoLiveData.postValue(playParams)
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

    private suspend fun findAndDownloadDanmu(
        davResource: DavResource,
        header: Map<String, String>
    ): String? {
        return withContext(Dispatchers.IO) {
            //目标文件名
            val targetFileName = getFileNameNoExtension(davResource.name) + ".xml"
            val fileList = fileLiveData.value ?: return@withContext null
            //遍历当前目录
            val danmuDavSource =
                fileList.find { it.name == targetFileName } ?: return@withContext null

            //下载文件
            val url = addressUrl + danmuDavSource.href.toASCIIString()
            try {
                val responseBody = Retrofit.extService.downloadResource(url, header)
                return@withContext DanmuUtils.saveDanmu(
                    danmuDavSource.name,
                    responseBody.byteStream()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext null
        }
    }

    private suspend fun findAndDownloadSubtitle(
        davResource: DavResource,
        header: Map<String, String>
    ): String? {
        return withContext(Dispatchers.IO) {
            //视频文件名
            val videoFileName = getFileNameNoExtension(davResource.name) + "."
            val fileList = fileLiveData.value ?: return@withContext null
            //遍历当前目录
            val subtitleDavSource =
                fileList.find {
                    SubtitleUtils.isSameNameSubtitle(it.name, videoFileName)
                } ?: return@withContext null
            //下载文件
            val url = addressUrl + subtitleDavSource.href.toASCIIString()
            try {
                val responseBody = Retrofit.extService.downloadResource(url, header)
                return@withContext SubtitleUtils.saveSubtitle(
                    subtitleDavSource.name,
                    responseBody.byteStream()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null
        }
    }

    private suspend fun matchAndDownloadDanmu(
        davResource: DavResource,
        header: Map<String, String>
    ): Pair<String, Int>? {
        return withContext(Dispatchers.IO) {
            val url = addressUrl + davResource.href.toASCIIString()
            var hash: String? = null
            try {
                //目标长度为前16M，17是容错
                val httpHelper = HashMap(header)
                httpHelper["range"] = "bytes=0-${17 * 1024 * 1024}"
                val responseBody = Retrofit.extService.downloadResource(url, header)
                hash = FileHashUtils.getHash(responseBody.byteStream())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (hash.isNullOrEmpty()) {
                return@withContext null
            }
            return@withContext DanmuUtils.matchDanmuSilence(viewModelScope, davResource.name, hash)
        }
    }
}