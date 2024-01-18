package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.extension.aesEncode
import com.xyoye.common_component.extension.authorizationValue
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.network.repository.ScreencastRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.ScreencastStorageFile
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.danmu.DanmuFinder
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.subtitle.SubtitleUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.data.DanmuEpisodeData
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

    override suspend fun cacheDanmu(file: StorageFile): LocalDanmuBean? {
        val videoData = (file as ScreencastStorageFile).getRealFile()
        val danmuUrl = screencastData?.getDanmuUrl(videoData.videoIndex)
            ?: return null
        val stream = ResourceRepository.getResourceResponseBody(danmuUrl).dataOrNull?.byteStream()
            ?: return null
        val episode = DanmuEpisodeData(
            animeTitle = "screencast",
            episodeTitle = getFileNameNoExtension(file.fileName())
        )
        return DanmuFinder.instance.saveStream(episode, stream)
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        val videoData = (file as ScreencastStorageFile).getRealFile()
        val subtitleUrl = screencastData?.getSubtitleUrl(videoData.videoIndex)
            ?: return null

        try {
            val result = ResourceRepository.getResourceResponse(subtitleUrl)
            val response = result.dataOrNull ?: return null
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
        val result = ScreencastRepository.init(
            "http://${library.screencastAddress}:${library.port}",
            library.password?.aesEncode()?.authorizationValue()
        )
        if (result is Response.Error) {
            ToastCenter.showError(result.error.toastMsg)
            return false
        }
        return true
    }

    fun setupScreencastData(data: ScreencastData) {
        screencastData = data
        directoryFiles = data.videos.map {
            ScreencastStorageFile(this, it.videoIndex, data)
        }
    }
}