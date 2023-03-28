package com.xyoye.common_component.storage.file.impl

import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.VideoStorage
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.getDirPath
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.entity.VideoEntity

/**
 * Created by xyoye on 2023/3/22
 */

class VideoStorageFile(
    storage: VideoStorage,
    private val entity: Any
) : AbstractStorageFile(storage) {
    override fun getRealFile(): Any {
        return entity
    }

    override fun filePath(): String {
        return when (entity) {
            is FolderBean -> {
                entity.folderPath
            }
            is VideoEntity -> {
                entity.filePath
            }
            else -> {
                ""
            }
        }
    }

    override fun fileUrl(): String {
        return when (entity) {
            is FolderBean -> {
                entity.folderPath
            }
            is VideoEntity -> {
                IOUtils.getVideoUri(entity.fileId).toString()
            }
            else -> {
                ""
            }
        }
    }

    override fun isDirectory(): Boolean {
        return entity is FolderBean
    }

    override fun fileName(): String {
        return when (entity) {
            is FolderBean -> {
                getFileName(entity.folderPath)
            }
            is VideoEntity -> {
                getFileName(entity.filePath)
            }
            else -> {
                ""
            }
        }
    }

    override fun clone(): StorageFile {
        return VideoStorageFile(
            storage as VideoStorage, entity
        ).also {
            it.playHistory = playHistory
        }
    }

    override fun fileLength(): Long {
        if (entity is VideoEntity) {
            return entity.fileLength
        }
        return 0
    }

    override fun childFileCount(): Int {
        if (entity is FolderBean) {
            return entity.fileCount
        }
        return 0
    }

    override fun uniqueKey(): String {
        if (entity is VideoEntity) {
            return entity.filePath.toMd5String()
        }
        return ""
    }

    override fun isVideoFile(): Boolean {
        return entity is VideoEntity && com.xyoye.common_component.utils.isVideoFile(fileName())
    }

    override fun isStoragePathParent(childPath: String): Boolean {
        return filePath() == getDirPath(childPath)
    }
}
