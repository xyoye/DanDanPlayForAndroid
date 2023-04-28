package com.xyoye.common_component.storage.file.helper

import com.xunlei.downloadlib.parameter.TorrentInfo

/**
 * Created by xyoye on 2023/4/6
 */

class TorrentBean(
    val torrentPath: String
) : TorrentInfo() {
    companion object {
        fun formInfo(torrentPath: String, info: TorrentInfo): TorrentBean {
            return TorrentBean(torrentPath).apply {
                mFileCount = info.mFileCount
                mInfoHash = info.mInfoHash
                mIsMultiFiles = info.mIsMultiFiles
                mMultiFileBaseFolder = info.mMultiFileBaseFolder
                mSubFileInfo = info.mSubFileInfo.onEach { it.mSubPath = torrentPath }
            }
        }
    }
}