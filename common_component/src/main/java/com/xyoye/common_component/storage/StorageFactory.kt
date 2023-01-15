package com.xyoye.common_component.storage

import com.xyoye.common_component.storage.impl.DocumentFileStorage
import com.xyoye.common_component.storage.impl.SmbStorage
import com.xyoye.common_component.storage.impl.WebDavStorage
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2022/12/29
 */

object StorageFactory {

    fun createStorage(library: MediaLibraryEntity): Storage? {
        return when (library.mediaType) {
            MediaType.EXTERNAL_STORAGE -> DocumentFileStorage(library)
            MediaType.WEBDAV_SERVER -> WebDavStorage(library)
            MediaType.SMB_SERVER -> SmbStorage(library)
            else -> null
        }
    }
}