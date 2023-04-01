package com.xyoye.common_component.storage.file.impl

import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.RemoteStorage
import com.xyoye.data_component.data.remote.RemoteVideoData

/**
 * Created by xyoye on 2023/4/1.
 */

class RemoteStorageFile(
    storage: RemoteStorage,
    private val videoData: RemoteVideoData
) : AbstractStorageFile(storage) {

    override fun getRealFile(): RemoteVideoData {
        return videoData
    }

    override fun filePath(): String {
        return videoData.absolutePath
    }

    override fun fileUrl(): String {
        if (videoData.absolutePath == "/" && videoData.isFolder) {
            return storage.rootUri.toString()
        }
        return storage.rootUri
            .buildUpon()
            .path("/api/v1/stream/id/${videoData.Id}")
            .toString()
    }

    override fun isDirectory(): Boolean {
        return videoData.isFolder
    }

    override fun fileName(): String {
        return videoData.getEpisodeName()
    }

    override fun fileLength(): Long {
        if (isDirectory())
            return 0
        return videoData.Size
    }

    override fun clone(): StorageFile {
        return RemoteStorageFile(
            storage as RemoteStorage,
            videoData,
        ).also { it.playHistory = playHistory }
    }

    override fun uniqueKey(): String {
        return videoData.Hash.ifEmpty { videoData.absolutePath }.toMd5String()
    }

    override fun childFileCount(): Int {
        return videoData.childData.size
    }

    override fun isVideoFile(): Boolean {
        return isFile()
    }
}