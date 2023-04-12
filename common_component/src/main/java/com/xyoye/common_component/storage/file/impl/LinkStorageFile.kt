package com.xyoye.common_component.storage.file.impl

import android.net.Uri
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.LinkStorage
import com.xyoye.common_component.utils.getFileName

/**
 * Created by xyoye on 2023/4/12
 */

class LinkStorageFile(
    storage: LinkStorage,
    private val url: String
) : AbstractStorageFile(storage) {
    override fun getRealFile(): Any {
        return url
    }

    override fun filePath(): String {
        return url
    }

    override fun fileUrl(): String {
        return Uri.parse(url).toString()
    }

    override fun isDirectory(): Boolean {
        return false
    }

    override fun fileName(): String {
        return getFileName(url)
    }

    override fun fileLength(): Long {
        return 0
    }

    override fun clone(): StorageFile {
        return LinkStorageFile(storage as LinkStorage, url).also {
            it.playHistory = playHistory
        }
    }

    override fun isVideoFile(): Boolean {
        return true
    }

    override fun uniqueKey(): String {
        return url.toMd5String()
    }
}