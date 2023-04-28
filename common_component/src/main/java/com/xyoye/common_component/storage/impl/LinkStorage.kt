package com.xyoye.common_component.storage.impl

import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.LinkStorageFile
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.InputStream

/**
 * Created by xyoye on 2023/4/12
 */

class LinkStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    private val httpHeaders = mutableMapOf<String, String>()

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        return emptyList()
    }

    override suspend fun getRootFile(): StorageFile {
        return LinkStorageFile(this, library.url)
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        return null
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        return null
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile {
        return LinkStorageFile(this, history.url).also {
            it.playHistory = history
        }
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        return file.fileUrl()
    }

    override fun getNetworkHeaders(): Map<String, String> {
        return httpHeaders
    }

    fun setupHttpHeader(headers: Map<String, String>?) {
        httpHeaders.clear()
        if (headers != null && headers.isNotEmpty()) {
            httpHeaders.putAll(headers)
        }
    }
}