package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestError
import com.xyoye.common_component.network.request.RequestErrorHandler
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.ScreencastStorageFile
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.data.screeencast.ScreencastVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream

/**
 * Created by xyoye on 2023/4/12
 */

class ScreencastStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    private var screencastData: ScreencastData? = null

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        val data = screencastData ?: return emptyList()
        return data.videos.map {
            ScreencastStorageFile(this, it.videoIndex, data)
        }
    }

    override suspend fun getRootFile(): StorageFile? {
        val data = screencastData ?: return null
        return ScreencastStorageFile(this, data.playIndex, data)
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        return null
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        return null
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile {
        val uri = Uri.parse(history.url)
        val index = uri.getQueryParameter("index")?.toIntOrNull() ?: 0
        val httpHeader = history.httpHeader?.run { JsonHelper.parseJsonMap(this) }
        val screencastData = ScreencastData(
            uri.port, uri.host,
            index, history.mediaType.value, httpHeader,
            listOf(ScreencastVideoData(index, history.videoName)),
            history.uniqueKey,
        )
        return ScreencastStorageFile(this, index, screencastData)
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        return file.fileUrl()
    }

    override fun getNetworkHeaders(): Map<String, String>? {
        return screencastData?.httpHeader
    }

    override suspend fun cacheDanmu(file: StorageFile): String? {
        val videoData = (file as ScreencastStorageFile).getRealFile()
        val danmuUrl = screencastData?.getDanmuUrl(videoData.videoIndex)
            ?: return null
        try {
            return DanmuUtils.saveDanmu(
                fileName = getFileNameNoExtension(file.fileName()) + ".xml",
                inputStream = Retrofit.remoteService.downloadDanmu(danmuUrl).byteStream(),
                directoryName = "screencast"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        val videoData = (file as ScreencastStorageFile).getRealFile()
        val subtitleUrl = screencastData?.getSubtitleUrl(videoData.videoIndex)
            ?: return null

        try {
            val response = Retrofit.extService.downloadResourceWithHeader(subtitleUrl)
            if (response.code() != NanoHTTPD.Response.Status.OK.requestStatus) return null
            val responseBody = response.body() ?: return null

            //下载并使用投屏字幕
            val subtitleSuffix = response.headers()["subtitleSuffix"] ?: "ass"
            val subtitleFileName = "${getFileNameNoExtension(file.fileName())}.${subtitleSuffix}"
            return SubtitleUtils.saveSubtitle(
                subtitleFileName,
                responseBody.byteStream(),
                directoryName = "screencast"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override suspend fun test(): Boolean {
        try {
            val result = Retrofit.screencastService.init(
                host = library.screencastAddress,
                port = library.port,
                authorization = library.password
            )
            if (result.success) {
                return true
            }
            val error = RequestError(result.errorCode, result.errorMessage ?: "未知错误")
            ToastCenter.showError("x${error.code} ${error.msg}")
        } catch (e: Exception) {
            e.printStackTrace()
            val error = RequestErrorHandler(e).handlerError()
            ToastCenter.showError("x${error.code} ${error.msg}")
        }
        return false
    }

    fun setupScreencastData(data: ScreencastData) {
        screencastData = data
        directoryFiles = data.videos.map {
            ScreencastStorageFile(this, it.videoIndex, data)
        }
    }
}