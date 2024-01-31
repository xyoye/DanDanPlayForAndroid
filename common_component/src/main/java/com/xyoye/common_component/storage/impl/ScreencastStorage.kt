package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.aesEncode
import com.xyoye.common_component.extension.authorizationValue
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.network.repository.ScreencastRepository
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.ScreencastStorageFile
import com.xyoye.common_component.storage.helper.ScreencastConstants
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.danmu.DanmuFinder
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.subtitle.SubtitleUtils
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
        return directoryFiles
    }

    override suspend fun getRootFile(): StorageFile? {
        return screencastData?.let { data ->
            directoryFiles.firstOrNull { file ->
                file.uniqueKey() == data.playUniqueKey
            }
        }
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        return null
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        return null
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile {
        val uri = Uri.parse(history.url)
        val httpHeader = history.httpHeader?.run { JsonHelper.parseJsonMap(this) }

        val videoData = ScreencastVideoData(
            history.videoName,
            history.uniqueKey,
            history.episodeId,
            history.danmuPath?.let { getFileName(it) },
            history.subtitlePath?.let { getFileName(it) },
            history.videoPosition,
            history.videoDuration
        )

        val screencastData = ScreencastData(
            uri.port,
            listOf(videoData),
            history.uniqueKey,
            httpHeader,
        ).apply {
            ip = uri.host
        }
        return ScreencastStorageFile(this, screencastData, videoData)
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        return file.fileUrl()
    }

    override fun getNetworkHeaders(): Map<String, String>? {
        return screencastData?.httpHeader
    }

    override suspend fun cacheDanmu(file: StorageFile): LocalDanmuBean? {
        val videoData = (file as ScreencastStorageFile).getRealFile()
        val danmuFileName = videoData.danmuFileName
        if (danmuFileName.isNullOrEmpty()) {
            return null
        }

        val danmuUrl = screencastData?.let { ScreencastConstants.ProviderApi.DANMU.buildUrl(it, videoData) }
            ?: return null
        val stream = ResourceRepository.getResourceResponseBody(danmuUrl).getOrNull()?.byteStream()
            ?: return null
        val episode = DanmuEpisodeData(
            animeTitle = "screencast",
            episodeId = videoData.episodeId.orEmpty(),
            episodeTitle = getFileNameNoExtension(danmuFileName)
        )
        return DanmuFinder.instance.saveStream(episode, stream)
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        val videoData = (file as ScreencastStorageFile).getRealFile()
        val subtitleFileName = videoData.subtitleFileName
        if (subtitleFileName.isNullOrEmpty()) {
            return null
        }

        val subtitleUrl = screencastData?.let { ScreencastConstants.ProviderApi.SUBTITLE.buildUrl(it, videoData) }
            ?: return null

        try {
            val result = ResourceRepository.getResourceResponse(subtitleUrl)
            val response = result.getOrNull() ?: return null
            if (response.code() != NanoHTTPD.Response.Status.OK.requestStatus) return null
            val responseBody = response.body() ?: return null

            //下载并使用投屏字幕
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
        if (result.isFailure) {
            result.exceptionOrNull()?.message?.toastError()
            return false
        }
        return true
    }

    /**
     * 绑定投屏数据
     */
    suspend fun bindScreencastData(data: ScreencastData) {
        screencastData = data
        directoryFiles = data.relatedVideos.map {
            ScreencastStorageFile(this, data, it).apply {
                playHistory = findPlayHistory(it)
            }
        }
    }

    /**
     * 寻找视频的播放记录
     */
    private suspend fun findPlayHistory(
        videoData: ScreencastVideoData
    ): PlayHistoryEntity? {
        val playHistory = DatabaseManager.instance
            .getPlayHistoryDao()
            .getPlayHistory(videoData.uniqueKey, library.id)

        // 播放进度优先级，投屏资源进度 > 历史记录进度
        if (videoData.position == 0L && videoData.duration == 0L) {
            return playHistory
        }

        val newHistory = playHistory ?: PlayHistoryEntity(
            0,
            "",
            "",
            mediaType = library.mediaType,
            uniqueKey = videoData.uniqueKey,
            storageId = library.id,
        )
        return newHistory.copy(
            videoPosition = videoData.position,
            videoDuration = videoData.duration
        ).also {
            DatabaseManager.instance.getPlayHistoryDao().insert(it)
        }
    }
}