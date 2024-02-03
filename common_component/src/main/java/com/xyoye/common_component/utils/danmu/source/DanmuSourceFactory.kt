package com.xyoye.common_component.utils.danmu.source

import android.net.Uri
import com.xyoye.common_component.extension.resourceType
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.DocumentStorageFile
import com.xyoye.common_component.storage.file.impl.VideoStorageFile
import com.xyoye.common_component.utils.danmu.source.impl.DocumentFileDanmuSource
import com.xyoye.common_component.utils.danmu.source.impl.IOFileDanmuSource
import com.xyoye.common_component.utils.danmu.source.impl.NetworkDanmuSource
import com.xyoye.data_component.enums.ResourceType

/**
 * Created by xyoye on 2023/12/26
 */

object DanmuSourceFactory {

    fun build(source: BaseVideoSource): DanmuSource? {
        return build(source.getVideoUrl(), source.getHttpHeader() ?: emptyMap())
    }

    fun build(storageFile: StorageFile): DanmuSource? {
        val url = if (storageFile is VideoStorageFile || storageFile is DocumentStorageFile) {
            storageFile.filePath()
        } else {
            null
        }
        return build(url.orEmpty())
    }

    fun build(url: String, headers: Map<String, String> = emptyMap()): DanmuSource? {
        return when (url.resourceType()) {
            ResourceType.File -> IOFileDanmuSource(url)
            ResourceType.URI -> DocumentFileDanmuSource(Uri.parse(url))
            ResourceType.URL -> NetworkDanmuSource(url, headers)
            else -> null
        }
    }
}