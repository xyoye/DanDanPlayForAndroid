package com.xyoye.common_component.storage

import com.xyoye.common_component.storage.impl.AlistStorage
import com.xyoye.common_component.storage.impl.DocumentFileStorage
import com.xyoye.common_component.storage.impl.FtpStorage
import com.xyoye.common_component.storage.impl.LinkStorage
import com.xyoye.common_component.storage.impl.RemoteStorage
import com.xyoye.common_component.storage.impl.ScreencastStorage
import com.xyoye.common_component.storage.impl.SmbStorage
import com.xyoye.common_component.storage.impl.TorrentStorage
import com.xyoye.common_component.storage.impl.VideoStorage
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
            MediaType.FTP_SERVER -> FtpStorage(library)
            MediaType.LOCAL_STORAGE -> VideoStorage(library)
            MediaType.REMOTE_STORAGE -> RemoteStorage(library)
            MediaType.MAGNET_LINK -> TorrentStorage(library)
            MediaType.STREAM_LINK -> LinkStorage(library)
            MediaType.OTHER_STORAGE -> LinkStorage(library)
            MediaType.SCREEN_CAST -> ScreencastStorage(library)
            MediaType.ALSIT_STORAGE -> AlistStorage(library)
            else -> null
        }
    }
}