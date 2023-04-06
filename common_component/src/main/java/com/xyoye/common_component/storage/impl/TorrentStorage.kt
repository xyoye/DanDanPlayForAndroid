package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xunlei.downloadlib.XLTaskHelper
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

    private var torrentBean: TorrentBean? = null

    init {
        PlayTaskManager.init()
    }

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        val directoryInfo = (file as TorrentStorageFile).getRealFile()
        val torrentPath = torrentPath(directoryInfo.mSubPath)
        if (torrentPath.isNullOrEmpty()) {
            ToastCenter.showError("获取种子文件失败")
            return emptyList()
        }
        val torrent = TorrentBean.formInfo(
            torrentPath,
            XLTaskHelper.getInstance().getTorrentInfo(torrentPath)
        ).also { torrentBean = it }
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

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile {
        return TorrentStorageFile(
            this,
            TorrentFileInfo().apply {
                mFileIndex = -1
                mSubPath = history.torrentPath
            }
        )
    }

    override suspend fun createPlayUrl(file: StorageFile): String? {
        val torrent = torrentBean ?: return null
        val fileIndex = (file as TorrentStorageFile).getRealFile().mFileIndex
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
}