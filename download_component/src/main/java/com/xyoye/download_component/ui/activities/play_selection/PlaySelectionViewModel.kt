package com.xyoye.download_component.ui.activities.play_selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.TorrentFileInfo
import com.xunlei.downloadlib.parameter.TorrentInfo
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.isValid
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.factory.TorrentSourceFactory
import com.xyoye.common_component.utils.FileComparator
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.isVideoFile
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.StorageFileBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PlaySelectionViewModel : BaseViewModel() {
    val finishLiveData = MutableLiveData<Boolean>()
    val fileLiveData = MutableLiveData<List<StorageFileBean>>()
    val playLiveData = MutableLiveData<Any>()
    val castLiveData = MutableLiveData<MediaLibraryEntity>()

    private lateinit var mTorrentPath: String

    private val curDirectoryFiles = mutableListOf<TorrentFileInfo>()

    fun initTorrentFiles(magnetLink: String?, torrentPath: String?) {
        viewModelScope.launch {
            showLoading()
            if (torrentPath != null && File(torrentPath).isValid()) {
                mTorrentPath = torrentPath
                readTorrentInfoFormFile()
                return@launch
            }

            if (magnetLink.isNullOrEmpty()) {
                ToastCenter.showError("无法找到资源内容")
                finishLiveData.postValue(true)
                return@launch
            }

            val torrentFilePath = ThunderManager.getInstance().downloadTorrentFile(
                magnetLink,
                PathHelper.getTorrentDirectory()
            )
            if (torrentFilePath.isNullOrEmpty()) {
                ToastCenter.showError("种子文件下载失败，请重试")
                finishLiveData.postValue(true)
                return@launch
            }
            mTorrentPath = torrentFilePath

            readTorrentInfoFormFile()
        }
    }

    fun refreshDirectoryWithHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val displayFiles = mutableListOf<StorageFileBean>()
            for ((index, info) in curDirectoryFiles.withIndex()) {
                val uniqueKey = TorrentSourceFactory.generateUniqueKey(mTorrentPath, index)
                val history = DatabaseManager.instance
                    .getPlayHistoryDao()
                    .getPlayHistory(uniqueKey, MediaType.MAGNET_LINK)
                val fileBean = StorageFileBean(
                    false,
                    "",
                    info.mFileName,
                    history?.danmuPath,
                    history?.subtitlePath,
                    history?.videoPosition ?: 0,
                    history?.videoDuration ?: 0,
                    uniqueKey,
                    lastPlayTime = history?.playTime
                )
                displayFiles.add(fileBean)
            }
            fileLiveData.postValue(displayFiles)
        }
    }

    fun playItem(uniqueKey: String) {
        viewModelScope.launch {
            if (setupVideoSource(uniqueKey)) {
                playLiveData.postValue(Any())
            }
        }
    }

    fun castItem(uniqueKey: String, device: MediaLibraryEntity) {
        viewModelScope.launch {
            if (setupVideoSource(uniqueKey)) {
                castLiveData.postValue(device)
            }
        }
    }

    private suspend fun setupVideoSource(uniqueKey: String): Boolean {
        var targetIndex = -1
        for (index in curDirectoryFiles.indices) {
            if (TorrentSourceFactory.generateUniqueKey(mTorrentPath, index) == uniqueKey) {
                targetIndex = index
                break
            }
        }
        if (targetIndex < 0) {
            ToastCenter.showError("播放失败，找不到播放资源")
            return false
        }

        showLoading()
        val mediaSource = VideoSourceFactory.Builder()
            .setRootPath(mTorrentPath)
            .setIndex(targetIndex)
            .create(MediaType.MAGNET_LINK)
        hideLoading()

        if (mediaSource == null) {
            ToastCenter.showError("资源无法播放，请更换其它资源")
            return false
        }

        VideoSourceManager.getInstance().setSource(mediaSource)
        return true
    }

    private fun readTorrentInfoFormFile() {
        val torrentInfo = getTorrentInfo(mTorrentPath)
        if (torrentInfo == null) {
            finishLiveData.postValue(true)
            return
        }

        val torrentInfoFiles = torrentInfo.mSubFileInfo
            .filter {
                isVideoFile(it.mFileName)
            }.sortedWith(FileComparator<TorrentFileInfo>(
                value = { it.mFileName },
                isDirectory = { false }
            )).map {
                it.apply { it.checked = false }
            }

        if (torrentInfoFiles.isEmpty()) {
            ToastCenter.showError("当前资源内无可播放的视频文件")
            finishLiveData.postValue(true)
            return
        }

        curDirectoryFiles.clear()
        curDirectoryFiles.addAll(torrentInfoFiles)

        hideLoading()
        refreshDirectoryWithHistory()
    }

    private fun getTorrentInfo(filePath: String): TorrentInfo? {
        if (filePath.isEmpty()) {
            ToastCenter.showError("文件路径为空")
            return null
        }

        val torrentFile = File(filePath)
        if (!torrentFile.exists() || !torrentFile.canRead()) {
            ToastCenter.showError("文件不存在或无法访问：${filePath}")
            return null
        }

        val torrentInfo = XLTaskHelper.getInstance().getTorrentInfo(filePath)
        if (torrentInfo?.mSubFileInfo.isNullOrEmpty()) {
            ToastCenter.showError("解析种子文件失败")
            return null
        }

        return torrentInfo
    }
}