package com.xyoye.download_component.ui.activities.download_list

import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.download_component.frostwire.TransferManager
import com.xyoye.download_component.frostwire.download.TorrentDownloader

class DownloadListViewModel : BaseViewModel() {
    private val mTransferManager = TransferManager.getInstance()

    val transferLiveData = mTransferManager.observerDownload()

    fun startTransfer() {
        mTransferManager.start()
    }

    fun addDownload(torrentPath: String, selection: ByteArray?) {
        mTransferManager.download(torrentPath, selection)
    }

    fun stopTransfer() {
        mTransferManager.stop()
    }

    fun removeDownload(downloader: TorrentDownloader, deleteData: Boolean) {
        mTransferManager.removeDownload(downloader, deleteData)
    }

    fun removeFinished() {
        mTransferManager.removeFinished()
    }
}