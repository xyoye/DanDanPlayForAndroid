package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestErrorHandler
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.helper.RemoteFileHelper
import com.xyoye.common_component.storage.file.impl.RemoteStorageFile
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import retrofit2.HttpException
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by xyoye on 2023/4/1.
 */

class RemoteStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    private var storageFilesSnapshot = listOf<RemoteVideoData>()

    init {
        val remoteUrl = "http://${library.url}:${library.port}/"
        val remoteToken = library.remoteSecret
        RemoteHelper.getInstance().remoteUrl = remoteUrl
        RemoteHelper.getInstance().remoteToken = remoteToken
    }

    override var rootUri: Uri = Uri.parse("http://${library.url}:${library.port}")

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        return if (file.isRootFile()) {
            getRemoteRootFiles() ?: emptyList()
        } else {
            (file as RemoteStorageFile).getRealFile().childData
        }.map { RemoteStorageFile(this, it) }
    }

    override suspend fun getRootFile(): StorageFile {
        val videoData = RemoteVideoData(absolutePath = "/", isFolder = true)
        return RemoteStorageFile(this, videoData)
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        return null
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        return null
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile? {
        val videoId = Uri.parse(history.url).lastPathSegment ?: return null
        return storageFilesSnapshot
            .firstOrNull { it.Id == videoId }
            ?.run { RemoteStorageFile(this@RemoteStorage, this) }
            ?.also { it.playHistory = history }
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        val videoId = (file as RemoteStorageFile).getRealFile().Id
        return RemoteHelper.getInstance().buildVideoUrl(videoId)
    }

    override suspend fun cacheDanmu(file: StorageFile): String? {
        try {
            val videoData = (file as RemoteStorageFile).getRealFile()
            return DanmuUtils.saveDanmu(
                fileName = getFileNameNoExtension(videoData.getEpisodeName()) + ".xml",
                inputStream = Retrofit.remoteService.downloadDanmu(videoData.Hash).byteStream(),
                directoryName = getParentFolderName(videoData.absolutePath)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        try {
            val videoData = (file as RemoteStorageFile).getRealFile()
            val subtitleData = Retrofit.remoteService.searchSubtitle(videoData.Id)
            if (subtitleData.subtitles.isNotEmpty()) {
                val fileName = subtitleData.subtitles.first().fileName
                return SubtitleUtils.saveSubtitle(
                    fileName,
                    Retrofit.remoteService.downloadSubtitle(videoData.Id, fileName).byteStream(),
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun supportSearch(): Boolean {
        return true
    }

    override suspend fun search(keyword: String): List<StorageFile> {
        if (keyword.isEmpty()) {
            return openDirectory(directory ?: getRootFile(), false)
        }
        return storageFilesSnapshot
            .filter { it.getEpisodeName().contains(keyword) || it.Path.contains(keyword) }
            .map { RemoteStorageFile(this, it) }
    }

    override suspend fun test(): Boolean {
        return getRemoteRootFiles() != null
    }

    private suspend fun getRemoteRootFiles(): List<RemoteVideoData>? {
        return try {
            val videoData = Retrofit.remoteService.openStorage().also {
                storageFilesSnapshot = it
            }
            return if (library.remoteAnimeGrouping) {
                RemoteFileHelper.convertGroupData(videoData)
            } else {
                RemoteFileHelper.convertTreeData(videoData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) {
                // ignore
            } else if (e is HttpException && e.code() == 401) {
                ToastCenter.showWarning("连接失败：密钥验证失败")
            } else {
                val error = RequestErrorHandler(e).handlerError()
                ToastCenter.showWarning("连接失败：${error.msg}")
            }
            null
        }
    }

}