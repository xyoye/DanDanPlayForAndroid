package com.xyoye.download_component.ui.activities.play_selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xunlei.downloadlib.XLDownloadManager
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.torrentFileIndex
import com.xyoye.common_component.extension.torrentPath
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.media.TorrentMediaSource
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.*
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicInteger

class PlaySelectionViewModel : BaseViewModel() {
    private val atomicInteger = AtomicInteger(0)

    val torrentDownloadLiveData = MutableLiveData<String>()
    val dismissLiveData = MutableLiveData<Boolean>()
    val playLiveData = MutableLiveData<Any>()

    fun downloadTorrentFile(magnetLink: String) {
        viewModelScope.launch {
            showLoading()
            val torrentFilePath = ThunderManager.getInstance().downloadTorrentFile(
                magnetLink,
                PathHelper.getDownloadTorrentDirectory()
            )
            hideLoading()

            if (torrentFilePath.isNullOrEmpty()) {
                ToastCenter.showError("种子文件下载失败，请重试")
                dismissLiveData.postValue(true)
                return@launch
            }

            torrentDownloadLiveData.postValue(torrentFilePath)
        }
    }

    fun torrentPlay(torrentPath: String, selectIndex: Int) {
        viewModelScope.launch {
            showLoading()
            val mediaSource = TorrentMediaSource.build(selectIndex, torrentPath)
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("启动播放任务失败，请重试")
                dismissLiveData.postValue(true)
                return@launch
            }

            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }

    fun playWithHistory(
        torrentPath: String,
        torrentFileIndex: Int,
        torrentTitle: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val (taskId, playUrl) = createTorrentTask(torrentPath, torrentFileIndex)
            if (playUrl.isNullOrEmpty()) {
                dismissLiveData.postValue(true)
                return@launch
            }

            var decodedUrl = URLDecoder.decode(playUrl, "utf-8")
            decodedUrl = URLDecoder.decode(decodedUrl, "utf-8")
            val videoTitle = getFileName(decodedUrl)

            val historyEntity = DatabaseManager.instance.getPlayHistoryDao()
                .findMagnetPlay(MediaType.MAGNET_LINK)
                .find {
                    it.torrentPath() == torrentPath && it.torrentFileIndex() == torrentFileIndex
                }

            val playParams = PlayParams(
                playUrl,
                videoTitle,
                historyEntity?.danmuPath,
                historyEntity?.subtitlePath,
                historyEntity?.videoPosition ?: 0,
                historyEntity?.episodeId ?: 0,
                MediaType.MAGNET_LINK
            ).apply {
                setPlayTaskId(taskId)
                setTorrentTitle(torrentTitle)
                setTorrentPath(torrentPath)
                setTorrentFileIndex(torrentFileIndex)
            }

            playLiveData.postValue(playParams)
        }
    }

    private suspend fun createTorrentTask(
        torrentPath: String,
        playIndex: Int
    ): Pair<Long, String?> {
        return withContext(Dispatchers.IO) {
            val playCacheDir = PathHelper.getPlayCacheDirectory()

            val playTaskParam = BtTaskParam().apply {
                setCreateMode(1)
                setFilePath(playCacheDir.absolutePath)
                setMaxConcurrent(1)
                setSeqId(atomicInteger.incrementAndGet())
                setTorrentPath(torrentPath)
            }

            //已选中索引
            val selectIndexSet = BtIndexSet(1)
            selectIndexSet.mIndexSet[0] = playIndex

            //未选中索引
            val torrentInfo = XLTaskHelper.getInstance().getTorrentInfo(torrentPath)
            val deSelectIndexSet: BtIndexSet
            if (torrentInfo.mFileCount > 1) {
                deSelectIndexSet = BtIndexSet(torrentInfo.mFileCount - 1)
                var deSelectIndex = 0
                torrentInfo.mSubFileInfo.forEach {
                    if (it.mFileIndex != playIndex) {
                        deSelectIndexSet.mIndexSet[deSelectIndex] = it.mFileIndex
                        deSelectIndex++
                    }
                }
            } else {
                deSelectIndexSet = BtIndexSet(0)
            }

            //启动下载任务
            val playTaskId =
                XLTaskHelper.getInstance()
                    .startTask(playTaskParam, selectIndexSet, deSelectIndexSet)
            if (playTaskId == -1L) {
                ToastCenter.showError("启动播放任务失败，请重试")
                return@withContext Pair(playTaskId, null)
            }

            val fileName = torrentInfo.mSubFileInfo[playIndex].mFileName
            val filePath = "${playTaskParam.mFilePath}/$fileName"
            val playUrl = XLTaskLocalUrl()
            XLDownloadManager.getInstance().getLocalUrl(filePath, playUrl)

            return@withContext Pair(playTaskId, playUrl.mStrUrl)
        }
    }
}