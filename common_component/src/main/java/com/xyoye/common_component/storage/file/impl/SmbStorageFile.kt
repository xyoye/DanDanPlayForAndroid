package com.xyoye.common_component.storage.file.impl

import android.net.Uri
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.SmbStorage

/**
 * Created by xyoye on 2023/1/14.
 */

class SmbStorageFile(
    storage: SmbStorage,
    private val shareName: String?,
    private val filePath: String,
    private val fileLength: Long = 0L,
    private val isDirectory: Boolean = true
) : AbstractStorageFile(storage) {
    override fun getRealFile(): Any {
        return Any()
    }

    override fun filePath(): String {
        return filePath
    }

    override fun fileUrl(): String {
        return storage.rootUri.buildUpon().path(filePath).build().toString()
    }

    override fun storagePath(): String {
        return "$shareName/$filePath"
    }

    override fun isDirectory(): Boolean {
        return isDirectory
    }

    override fun fileName(): String {
        if (filePath.isEmpty()) {
            return shareName ?: "unknown"
        }
        return Uri.parse(filePath).lastPathSegment ?: "unknown"
    }

    override fun fileLength(): Long {
        return fileLength
    }

    override fun clone(): StorageFile {
        return SmbStorageFile(
            storage as SmbStorage, shareName, filePath, fileLength, isDirectory
        ).also {
            it.playHistory = playHistory
        }
    }

    override fun isRootFile(): Boolean {
        return shareName == null
    }

    fun isShareDirectory() = shareName != null && filePath.isEmpty()

    fun getShareName() = shareName
}