package com.xyoye.common_component.storage.file.impl

import android.net.Uri
import com.xunlei.downloadlib.parameter.TorrentFileInfo
import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.TorrentStorage
import com.xyoye.common_component.utils.getFileNameNoExtension

/**
 * Created by xyoye on 2023/4/3
 */

class TorrentStorageFile(
    storage: TorrentStorage,
    private val fileInfo: TorrentFileInfo
) : AbstractStorageFile(storage) {
    override fun getRealFile(): TorrentFileInfo {
        return fileInfo
    }

    override fun filePath(): String {
        return fileInfo.mSubPath
    }

    override fun fileUrl(): String {
        return Uri.parse(fileInfo.mSubPath).toString()
    }

    override fun isDirectory(): Boolean {
        return fileInfo.mFileIndex == -1
    }

    override fun fileName(): String {
        return fileInfo.mFileName
    }

    override fun fileLength(): Long {
        return fileInfo.mFileSize
    }

    override fun clone(): StorageFile {
        return TorrentStorageFile(storage as TorrentStorage, fileInfo).also {
            it.playHistory = playHistory
        }
    }

    override fun uniqueKey(): String {
        val hash = getFileNameNoExtension(fileInfo.mSubPath)
        return (hash + "_" + fileInfo.mFileIndex).toMd5String()
    }
}