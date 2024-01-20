package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.network.repository.RemoteRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.helper.RemoteFileHelper
import com.xyoye.common_component.storage.file.impl.RemoteStorageFile
import com.xyoye.common_component.utils.danmu.DanmuFinder
import com.xyoye.common_component.utils.subtitle.SubtitleUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by xyoye on 2023/4/1.
 */

class RemoteStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    private var storageFilesSnapshot = listOf<RemoteVideoData>()

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
        val fileUrl = (file as RemoteStorageFile).fileUrl()
        val token = library.remoteSecret ?: return fileUrl

        return Uri.parse(fileUrl)
            .buildUpon()
            .appendQueryParameter("token", token)
            .build()
            .toString()
    }

    override suspend fun cacheDanmu(file: StorageFile): LocalDanmuBean? {
        val videoData = (file as RemoteStorageFile).getRealFile()
        val episode = DanmuEpisodeData(
            episodeId = videoData.Id,
            episodeTitle = videoData.EpisodeTitle,
            animeId = videoData.AnimeId,
            animeTitle = videoData.AnimeTitle
        )
        return DanmuFinder.instance.downloadEpisode(episode)
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        val videoData = (file as RemoteStorageFile).getRealFile()
        val result = RemoteRepository.getRelatedSubtitles(this, videoData.Id)

        val subtitleFileName = result.dataOrNull?.subtitles?.firstOrNull()?.fileName
            ?: return null

        return RemoteRepository.downloadSubtitle(this, videoData.Id, subtitleFileName)
            .dataOrNull
            ?.byteStream()
            ?.let { SubtitleUtils.saveSubtitle(subtitleFileName, it) }
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
        val result = RemoteRepository.getStorageFiles(this)
        if (result is Response.Error) {
            if (result.error.original is CancellationException) {
                // ignore
            } else if (result.error.code == 401) {
                ToastCenter.showWarning("连接失败：密钥验证失败")
            } else {
                ToastCenter.showWarning("连接失败：${result.error.msg}")
            }
            return null
        }

        return result.dataOrNull?.let {
            storageFilesSnapshot = it

            if (library.remoteAnimeGrouping) {
                RemoteFileHelper.convertGroupData(it)
            } else {
                RemoteFileHelper.convertTreeData(it)
            }
        }
    }
}