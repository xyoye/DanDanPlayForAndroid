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
import com.xyoye.common_component.utils.MagnetUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.bean.TorrentPlaySelectionBean
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.*
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicInteger

class PlaySelectionViewModel : BaseViewModel() {
    private val atomicInteger = AtomicInteger(0)

    val torrentDownloadLiveData = MutableLiveData<String>()
    val dismissLiveData = MutableLiveData<Boolean>()
    val playLiveData = MutableLiveData<PlayParams>()
    val preparePlayLiveData = MutableLiveData<TorrentPlaySelectionBean>()

    fun downloadTorrentFile(magnetLink: String) {
        val hash = MagnetUtils.getMagnetHash(magnetLink)
        if (hash.isEmpty()) {
            ToastCenter.showError("错误的Magnet链接")
            dismissLiveData.postValue(true)
            return
        }

        val torrentDir = PathHelper.getDownloadTorrentDirectory()

        val torrentTaskParam = MagnetTaskParam().apply {
            setFileName("$hash.torrent")
            setFilePath(torrentDir.absolutePath)
            setUrl("magnet:?xt=urn:btih:$hash")
        }
        //添加种子下载任务
        val torrentTaskId = XLTaskHelper.getInstance().addMagnetTask(torrentTaskParam)
        if (torrentTaskId == -1L) {
            ToastCenter.showError("磁链解析任务开启失败")
            dismissLiveData.postValue(true)
            return
        }

        viewModelScope.launch(context = Dispatchers.Main) {
            showLoading()
            val taskInfo = async(context = Dispatchers.IO) {
                //300ms查询一次下载状态
                var waitTime = 0
                var taskInfo = XLTaskHelper.getInstance().getTaskInfo(torrentTaskId)
                while (taskInfo.mTaskStatus == XLConstant.XLTaskStatus.TASK_IDLE
                    || taskInfo.mTaskStatus == XLConstant.XLTaskStatus.TASK_RUNNING
                ) {

                    //下载种子最多等待五秒
                    if (waitTime > 5 * 1000L)
                        break

                    delay(300L)
                    waitTime += 300
                    taskInfo = XLTaskHelper.getInstance().getTaskInfo(torrentTaskId)
                }
                XLTaskHelper.getInstance().stopTask(torrentTaskId)
                return@async taskInfo
            }.await()

            hideLoading()
            when (taskInfo.mTaskStatus) {
                XLConstant.XLTaskStatus.TASK_SUCCESS -> {
                    val torrentPath = "${torrentDir.absolutePath}/$hash.torrent"
                    torrentDownloadLiveData.postValue(torrentPath)
                }
                else -> {
                    ToastCenter.showError("种子文件下载失败，请重试")
                    dismissLiveData.postValue(true)
                }
            }
        }
    }

    fun prepareTorrentPlay(torrentPath: String, playIndex: Int) {
        viewModelScope.launch {
            val (taskId, playUrl) = createTorrentTask(torrentPath, playIndex)
            if (playUrl.isNullOrEmpty()) {
                dismissLiveData.postValue(true)
                return@launch
            }
            preparePlayLiveData.postValue(
                TorrentPlaySelectionBean(
                    taskId,
                    playUrl,
                    playIndex,
                    torrentPath
                )
            )
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