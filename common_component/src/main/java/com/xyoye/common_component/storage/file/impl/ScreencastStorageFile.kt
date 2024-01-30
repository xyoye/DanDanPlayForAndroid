package com.xyoye.common_component.storage.file.impl

import android.net.Uri
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.ScreencastStorage
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.data.screeencast.ScreencastVideoData

/**
 * Created by xyoye on 2023/4/12
 */

class ScreencastStorageFile(
    storage: ScreencastStorage,
    private val screencastData: ScreencastData,
    private val videoData: ScreencastVideoData,
) : AbstractStorageFile(storage) {
    override fun getRealFile(): ScreencastVideoData {
        return videoData
    }

    override fun filePath(): String {
        return screencastData.getVideoUrl(videoData)
    }

    override fun fileUrl(): String {
        return Uri.parse(filePath()).toString()
    }

    override fun isDirectory(): Boolean {
        return false
    }

    override fun fileName(): String {
        return videoData.title
    }

    override fun fileLength(): Long {
        return 0
    }

    override fun clone(): StorageFile {
        return ScreencastStorageFile(
            storage as ScreencastStorage,
            screencastData,
            videoData
        ).also { it.playHistory = playHistory }
    }

    override fun isVideoFile(): Boolean {
        return true
    }

    override fun uniqueKey(): String {
        return videoData.uniqueKey
    }
}