package com.xyoye.download_component.ui.activities.play_selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.*
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
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.*
import java.io.File

class PlaySelectionViewModel : BaseViewModel() {
    val finishLiveData = MutableLiveData<Boolean>()
    val fileLiveData = MutableLiveData<List<StorageFileBean>>()
    val playLiveData = MutableLiveData<Any>()

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
                    .getHistoryByKey(uniqueKey, MediaType.MAGNET_LINK)
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
            var targetIndex = -1
            for (index in curDirectoryFiles.indices) {
                if (TorrentSourceFactory.generateUniqueKey(mTorrentPath, index) == uniqueKey) {
                    targetIndex = index
                    break
                }
            }
            if (targetIndex < 0) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }

            showLoading()
            val mediaSource = VideoSourceFactory.Builder()
                .setRootPath(mTorrentPath)
                .setIndex(targetIndex)
                .create(MediaType.MAGNET_LINK)
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("启动播放任务失败，请重试")
                finishLiveData.postValue(true)
                return@launch
            }

            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
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