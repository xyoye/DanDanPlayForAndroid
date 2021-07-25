package com.xyoye.download_component.ui.activities.play_selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xunlei.downloadlib.XLDownloadManager
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.extraMap
import com.xyoye.common_component.utils.MagnetUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.*
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicInteger

class PlaySelectionViewModel : BaseViewModel() {
    private val taskIdList = mutableListOf<Long>()
    private val atomicInteger = AtomicInteger(0)

    val torrentDownloadLiveData = MutableLiveData<String>()
    val dismissLiveData = MutableLiveData<Boolean>()
    val playLiveData = MutableLiveData<PlayParams>()

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

    fun prepareTorrentPlay(torrentPath: String, playIndex: Int): String? {
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
            XLTaskHelper.getInstance().startTask(playTaskParam, selectIndexSet, deSelectIndexSet)
        if (playTaskId == -1L) {
            ToastCenter.showError("启动播放任务失败，请重试")
            return null
        }
        //加入下载任务集合，用于停止及删除任务
        taskIdList.add(playTaskId)

        val fileName = torrentInfo.mSubFileInfo[playIndex].mFileName
        val filePath = "${playTaskParam.mFilePath}/$fileName"
        val playUrl = XLTaskLocalUrl()
        XLDownloadManager.getInstance().getLocalUrl(filePath, playUrl)

        return playUrl.mStrUrl
    }

    fun removePlayTask() {
        GlobalScope.launch(Dispatchers.IO) {
            val playCacheDir = PathHelper.getPlayCacheDirectory()
            taskIdList.forEach {
                XLTaskHelper.getInstance().stopTask(it)
                XLTaskHelper.getInstance().deleteTask(it, playCacheDir.absolutePath)
            }
            playCacheDir.delete()
        }
    }

    fun playWithHistory(
        playUrl: String,
        torrentPath: String,
        torrentFileIndex: Int,
        torrentTitle: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var decodedUrl = URLDecoder.decode(playUrl, "utf-8")
            decodedUrl = URLDecoder.decode(decodedUrl, "utf-8")
            val videoTitle = getFileName(decodedUrl)

            val historyEntity = DatabaseManager.instance.getPlayHistoryDao()
                .findMagnetPlay(MediaType.MAGNET_LINK)
                .find {
                    val entityFileIndex = it.extraMap()["torrent_file_index"]?.toIntOrNull() ?: 0
                    it.extraMap()["torrent_path"] == torrentPath && entityFileIndex == torrentFileIndex
                }

            val extra = mapOf(
                Pair("torrent_path", torrentPath),
                Pair("torrent_file_index", torrentFileIndex.toString()),
                Pair("torrent_title", torrentTitle ?: "")
            )

            val playParams = PlayParams(
                playUrl,
                videoTitle,
                historyEntity?.danmuPath,
                historyEntity?.subtitlePath,
                historyEntity?.videoPosition ?: 0,
                historyEntity?.episodeId ?: 0,
                MediaType.MAGNET_LINK,
                extra
            )

            playLiveData.postValue(playParams)
        }
    }
}