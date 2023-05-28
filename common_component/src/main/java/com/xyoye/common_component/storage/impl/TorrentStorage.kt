package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xunlei.downloadlib.parameter.TorrentFileInfo
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.helper.PlayTaskManager
import com.xyoye.common_component.storage.file.helper.TorrentBean
import com.xyoye.common_component.storage.file.impl.TorrentStorageFile
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.InputStream

/**
 * Created by xyoye on 2023/4/3
 */

class TorrentStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    init {
        PlayTaskManager.init()
    }

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        val torrent = getTorrentFormFile(file)
        if (torrent == null) {
            ToastCenter.showError("获取种子文件失败")
            return emptyList()
        }
        if (torrent.mSubFileInfo.isNullOrEmpty()) {
            ToastCenter.showError("解析种子文件失败")
            return emptyList()
        }
        return torrent.mSubFileInfo.map {
            TorrentStorageFile(this, it)
        }
    }

    override suspend fun getRootFile(): StorageFile {
        return TorrentStorageFile(
            this,
            TorrentFileInfo().apply {
                mFileIndex = -1
                mSubPath = library.url
            }
        )
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        return null
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        return null
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile? {
        val torrentPath = history.torrentPath
            ?: return null
        val torrent = TorrentBean.formInfo(
            torrentPath,
            ThunderManager.getInstance().getTaskInfo(torrentPath)
        )

        val fileInfo = torrent.mSubFileInfo?.find {
            it.mFileIndex == history.torrentIndex
        } ?: return null

        return TorrentStorageFile(this, fileInfo).also {
            it.playHistory = history
        }
    }

    override suspend fun createPlayUrl(file: StorageFile): String? {
        val torrent = getTorrentFormFile(file)
            ?: return null
        val fileIndex = (file as TorrentStorageFile).getRealFile().mFileIndex
        if (fileIndex == -1) {
            return null
        }
        return ThunderManager.getInstance().generatePlayUrl(torrent, fileIndex)
    }

    override suspend fun cacheDanmu(file: StorageFile): String? {
        return null
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        return null
    }

    private suspend fun torrentPath(url: String): String? {
        val isMagnetLink = Uri.parse(url).scheme == "magnet"
        return if (isMagnetLink) {
            ThunderManager.getInstance().downloadTorrentFile(url)
        } else {
            url
        }
    }

    private suspend fun getTorrentFormFile(file: StorageFile): TorrentBean? {
        val directoryInfo = (file as TorrentStorageFile).getRealFile()
        val torrentPath = torrentPath(directoryInfo.mSubPath)
        if (torrentPath.isNullOrEmpty()) {
            return null
        }
        return TorrentBean.formInfo(
            torrentPath,
            ThunderManager.getInstance().getTaskInfo(torrentPath)
        )
    }
}