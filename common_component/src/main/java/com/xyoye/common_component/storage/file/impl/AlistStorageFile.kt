package com.xyoye.common_component.storage.file.impl

import android.net.Uri
import com.xyoye.common_component.extension.resourceType
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.AlistStorage
import com.xyoye.data_component.data.alist.AlistFileData
import com.xyoye.data_component.enums.ResourceType
import java.io.File

/**
 * Created by xyoye on 2024/1/20.
 */

class AlistStorageFile(
    private val parentPath: String,
    private val fileData: AlistFileData,
    storage: AlistStorage
) : AbstractStorageFile(storage) {
    override fun getRealFile(): Any {
        return fileData
    }

    override fun filePath(): String {
        val uriBuilder = Uri.Builder().encodedPath(parentPath)
        if (fileName().startsWith(File.separator)) {
            uriBuilder.encodedPath(fileName())
        } else {
            uriBuilder.appendEncodedPath(fileName())
        }
        return uriBuilder.build().toString()
    }

    override fun fileUrl(): String {
        return filePath()
    }

    override fun fileCover(): String? {
        if (isDirectory()) {
            return null
        }

        val cover = super.fileCover()
        if (cover?.isNotEmpty() == true) {
            return cover
        }

        if (fileData.thumb.resourceType() == ResourceType.URL) {
            return fileData.thumb
        }

        return null
    }

    override fun isDirectory(): Boolean {
        return fileData.isDirectory
    }

    override fun fileName(): String {
        return fileData.name
    }

    override fun fileLength(): Long {
        return fileData.size
    }

    override fun clone(): StorageFile {
        return AlistStorageFile(
            parentPath, fileData, storage as AlistStorage
        ).also {
            it.playHistory = playHistory
        }
    }

}