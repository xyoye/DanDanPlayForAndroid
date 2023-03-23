package com.xyoye.common_component.storage.impl

import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.VideoStorageFile
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import java.io.InputStream

/**
 * Created by xyoye on 2023/3/22
 */

class VideoStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
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

    override suspend fun getRootFile(): StorageFile {
        val folderBean = FolderBean(rootUri.toString(), 0)
        return VideoStorageFile(this, folderBean)
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        return null
    }

    override suspend fun pathFile(path: String): StorageFile? {
        return null
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        return file.filePath()
    }
}