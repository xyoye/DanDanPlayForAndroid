package com.xyoye.common_component.storage

import androidx.lifecycle.Lifecycle
import com.xyoye.common_component.storage.impl.*
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2022/12/29
 */

object StorageFactory {

    fun createStorage(library: MediaLibraryEntity, lifecycle: Lifecycle): Storage? {
        return when (library.mediaType) {
            MediaType.EXTERNAL_STORAGE -> DocumentFileStorage(library)
            MediaType.WEBDAV_SERVER -> WebDavStorage(library)
            MediaType.SMB_SERVER -> SmbStorage(library)
            MediaType.FTP_SERVER -> FtpStorage(library, lifecycle)
            MediaType.LOCAL_STORAGE -> VideoStorage(library)
            MediaType.REMOTE_STORAGE -> RemoteStorage(library)
            else -> null
        }
    }
}