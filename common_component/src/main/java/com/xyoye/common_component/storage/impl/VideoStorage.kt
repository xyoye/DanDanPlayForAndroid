package com.xyoye.common_component.storage.impl

import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.VideoStorageFile
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.entity.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by xyoye on 2023/3/22
 */

class VideoStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    override suspend fun openDirectory(file: StorageFile): List<StorageFile> {
        directory = file
        directoryFiles = listFiles(file)

        return if (file.isRootFile()) {
            DatabaseManager.instance
                .getVideoDao()
                .getFolderByFilter()
                .map { VideoStorageFile(this, it) }
        } else {
            DatabaseManager.instance
                .getVideoDao()
                .getVideoInFolder(file.filePath())
                .map { VideoStorageFile(this, it) }
        }
    }

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        if (file.isFile()) {
            return emptyList()
        }
        val filePath = file.filePath()
        val directory = File(filePath)
        if (directory.exists().not() || directory.canRead().not()) {
            return emptyList()
        }
        return directory.listFiles()?.map {
            VideoStorageFile(this, VideoEntity(fileId = 0, filePath = it.absolutePath, folderPath = filePath))
        } ?: emptyList()
    }

    override suspend fun getRootFile(): StorageFile {
        val folderBean = FolderBean(rootUri.toString(), 0)
        return VideoStorageFile(this, folderBean)
    }

    override suspend fun openFile(file: StorageFile): InputStream {
        return withContext(Dispatchers.IO) {
            FileInputStream(file.filePath())
        }
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        if (isDirectory.not()) {
            val videoEntity = DatabaseManager.instance.getVideoDao().getVideo(path)
                ?: return null
            return VideoStorageFile(this, videoEntity)
        }
        return null
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile? {
        val storagePath = history.storagePath ?: return null
        return pathFile(storagePath, false)?.also {
            it.playHistory = history
        }
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        return file.filePath()
    }

    override suspend fun cacheDanmu(file: StorageFile): String {
        return file.filePath()
    }

    override suspend fun cacheSubtitle(file: StorageFile): String {
        return file.filePath()
    }
}