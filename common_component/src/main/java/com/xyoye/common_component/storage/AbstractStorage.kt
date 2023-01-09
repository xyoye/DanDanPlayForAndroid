package com.xyoye.common_component.storage

import android.net.Uri
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.isVideoFile
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.File

/**
 * Created by xyoye on 2022/12/29
 */

abstract class AbstractStorage(
    libraryEntity: MediaLibraryEntity
) : Storage {

    override var library: MediaLibraryEntity = libraryEntity

    override var directory: StorageFile? = null

    override var directoryFiles: List<StorageFile> = emptyList()

    override var rootUri: Uri = Uri.parse(libraryEntity.url)

    override suspend fun openDirectory(file: StorageFile): List<StorageFile> {
        this.directory = file
        this.directoryFiles = listFiles(file)
        return directoryFiles
    }

    override fun close() {
        //do nothing
    }

    /**
     * 在根路径中定位到相对路径或绝对路径
     */
    protected fun resolvePath(path: String): Uri {
        return if (path.startsWith(File.separator)) {
            rootUri.buildUpon().path(path).build()
        } else {
            rootUri.buildUpon().appendPath(path).build()
        }
    }

    /**
     * 获取文件的播放记录
     */
    override suspend fun getPlayHistory(
        file: StorageFile
    ): PlayHistoryEntity? {
        if (file.isDirectory()) {
            return null
        }
        if (isVideoFile(file.fileName()).not()) {
            return null
        }
        return DatabaseManager.instance
            .getPlayHistoryDao()
            .getPlayHistory(
                file.uniqueKey(),
                library.mediaType
            )
    }

    override fun getNetworkHeaders(): Map<String, String>? {
        return null
    }

    abstract suspend fun listFiles(file: StorageFile): List<StorageFile>
}