package com.xyoye.common_component.storage.file.impl

import android.net.Uri
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.ScreencastStorage
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.data.screeencast.ScreencastVideoData
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2023/4/12
 */

class ScreencastStorageFile(
    storage: ScreencastStorage,
    private val videoIndex: Int,
    private val screencastData: ScreencastData,
) : AbstractStorageFile(storage) {
    override fun getRealFile(): ScreencastVideoData {
        return screencastData.videos[videoIndex]
    }

    override fun filePath(): String {
        return screencastData.getVideoUrl(videoIndex)
    }

    override fun fileUrl(): String {
        return Uri.parse(filePath()).toString()
    }

    override fun isDirectory(): Boolean {
        return false
    }

    override fun fileName(): String {
        return screencastData.videos[videoIndex].videoTitle
    }

    override fun fileLength(): Long {
        return 0
    }

    override fun clone(): StorageFile {
        return ScreencastStorageFile(
            storage as ScreencastStorage,
            videoIndex,
            screencastData
        ).also { it.playHistory = playHistory }
    }

    override fun isVideoFile(): Boolean {
        return true
    }

    override fun uniqueKey(): String {
        val originalUniqueKey = screencastData.getVideoUrl(videoIndex)
        return "${MediaType.SCREEN_CAST.value}_$originalUniqueKey".toMd5String()
    }
}