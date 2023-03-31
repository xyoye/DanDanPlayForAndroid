package com.xyoye.common_component.storage

import android.net.Uri
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getParentFolderName
import com.xyoye.data_component.entity.MediaLibraryEntity
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

    override suspend fun openDirectory(file: StorageFile, refresh: Boolean): List<StorageFile> {
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

    override fun getNetworkHeaders(): Map<String, String>? {
        return null
    }

    abstract suspend fun listFiles(file: StorageFile): List<StorageFile>

    override suspend fun cacheDanmu(file: StorageFile): String? {
        val inputStream = openFile(file) ?: return null
        val fileName = file.fileName()
        val directoryName = getParentFolderName(file.filePath())
        return DanmuUtils.saveDanmu(fileName, inputStream, directoryName)
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        val inputStream = openFile(file) ?: return null
        val fileName = file.fileName()
        val directoryName = getParentFolderName(file.filePath())
        return SubtitleUtils.saveSubtitle(fileName, inputStream, directoryName)
    }

    override fun supportGlobalSearch(): Boolean {
        return false
    }
}