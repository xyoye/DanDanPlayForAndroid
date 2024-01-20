package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.network.repository.AlistRepository
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.AlistStorageFile
import com.xyoye.data_component.data.alist.AlistFileData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.InputStream

/**
 * Created by xyoye on 2024/1/20.
 */

class AlistStorage(
    library: MediaLibraryEntity
) : AbstractStorage(library) {

    private var token: String = ""

    private val rootUrl by lazy { rootUri.toString() }

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        return AlistRepository.openDirectory(rootUrl, token, file.filePath())
            .dataOrNull
            ?.successData
            ?.fileList
            ?.map {
                AlistStorageFile(file.filePath(), it, this)
            } ?: emptyList()
    }

    override suspend fun getRootFile(): StorageFile? {
        val newToken = refreshToken() ?: return null
        this.token = newToken

        return AlistRepository.getRootFile(rootUrl, token).dataOrNull
            ?.successData
            ?.let {
                AlistFileData(it.rootPath, true)
            }?.let {
                AlistStorageFile("", it, this)
            }
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        val rawUrl = getStorageFileUrl(file)
            ?: return null

        return ResourceRepository.getResourceResponseBody(rawUrl)
            .dataOrNull
            ?.byteStream()
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        if (token.isEmpty()) {
            token = refreshToken() ?: return null
        }

        val pathUri = Uri.parse(path)
        val fileName = pathUri.lastPathSegment
        val parentPath = pathUri.path?.removeSuffix("/$fileName") ?: "/"
        return AlistRepository.openFile(rootUrl, token, path)
            .dataOrNull
            ?.successData
            ?.let {
                AlistStorageFile(parentPath, it, this)
            }
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile? {
        return history.storagePath
            ?.let { pathFile(it, false) }
            ?.also { it.playHistory = history }
    }

    override suspend fun createPlayUrl(file: StorageFile): String? {
        return getStorageFileUrl(file)
    }

    override suspend fun test(): Boolean {
        return refreshToken()?.isNotEmpty() == true
    }

    private suspend fun refreshToken(): String? {
        val username = library.account ?: return null
        val password = library.password ?: return null

        return AlistRepository.login(rootUrl, username, password)
            .dataOrNull
            ?.successData
            ?.token
    }

    private suspend fun getStorageFileUrl(file: StorageFile): String? {
        val rawUrl = file.getFile<AlistFileData>()?.rawUrl
        if (rawUrl?.isNotEmpty() == true) {
            return rawUrl
        }

        return AlistRepository.openFile(rootUrl, token, file.filePath())
            .dataOrNull
            ?.successData
            ?.rawUrl
    }
}