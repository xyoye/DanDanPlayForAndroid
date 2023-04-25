package com.xyoye.common_component.storage

import android.net.Uri
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.*
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
        val danmuFileName = getFileNameNoExtension(file.fileName()) + ".xml"
        val danmuFile = directoryFiles.find {
            it.isFile() && isDanmuFile(it.fileName()) && it.fileName() == danmuFileName
        } ?: return null

        val inputStream = openFile(danmuFile) ?: return null
        val directoryName = getParentFolderName(danmuFile.filePath())
        return DanmuUtils.saveDanmu(danmuFileName, inputStream, directoryName)
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        val videoFileName = getFileNameNoExtension(file.fileName()) + "."
        val subtitleFile = directoryFiles.find {
            it.isFile() && SubtitleUtils.isSameNameSubtitle(it.fileName(), videoFileName)
        } ?: return null

        val inputStream = openFile(subtitleFile) ?: return null
        val directoryName = getParentFolderName(subtitleFile.filePath())
        return SubtitleUtils.saveSubtitle(subtitleFile.fileName(), inputStream, directoryName)
    }

    override fun supportSearch(): Boolean {
        return false
    }

    override suspend fun search(keyword: String): List<StorageFile> {
        return emptyList()
    }

    override suspend fun test(): Boolean {
        return true
    }
}