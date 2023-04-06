package com.xyoye.common_component.utils.thunder

import com.xunlei.downloadlib.XLDownloadManager
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.*
import com.xyoye.common_component.storage.file.helper.TorrentBean
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.comparator.FileNameComparator
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by xyoye on 2021/11/20.
 */

class ThunderManager private constructor() {
    private val mAtomicInteger = AtomicInteger(0)
    private val mTaskList = hashMapOf<String, Long>()

    // 种子文件存储文件夹
    private val torrentDirectory = PathHelper.getTorrentDirectory()

    // 缓存文件存储文件夹
    private val cacheDirectory = PathHelper.getPlayCacheDirectory()

    companion object {
        private const val INVALID_ID = -1L
        private const val TIME_OUT_DOWNLOAD_TORRENT = 5 * 1000L

        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = ThunderManager()
    }

    fun getTaskInfo(torrentPath: String): TorrentInfo {
        return XLTaskHelper.getInstance().getTorrentInfo(torrentPath)
    }

    fun getTaskId(torrentPath: String): Long {
        return mTaskList[torrentPath] ?: INVALID_ID
    }

    /**
     * 下载种子文件
     * @param magnet 磁链
     * @return 种子文件路径
     */
    suspend fun downloadTorrentFile(magnet: String): String? {
        val hash = MagnetUtils.getMagnetHash(magnet)
        if (hash.isEmpty())
            return null

        val torrentTaskParam = MagnetTaskParam().apply {
            setFileName("$hash.torrent")
            setFilePath(torrentDirectory.absolutePath)
            setUrl("magnet:?xt=urn:btih:$hash")
        }

        val torrentTaskId = XLTaskHelper.getInstance().addMagnetTask(torrentTaskParam)
        if (torrentTaskId == INVALID_ID) {
            return null
        }

        return waitTorrentDownloaded(torrentTaskId, torrentTaskParam)
    }

    /**
     * 根据种子文件返回播放链接
     * @param torrentPath 种子文件路径
     * @param saveDirectory 下载文件路径
     * @param selectIndex 子文件位置索引
     * @return 播放链接, 视频文件列表
     */
    suspend fun torrent2PlayUrl(
        torrentPath: String,
        selectIndex: Int
    ): Pair<String?, List<TorrentFileInfo>> {
        val btTaskParam = BtTaskParam().apply {
            setCreateMode(1)
            setFilePath(cacheDirectory.absolutePath)
            setMaxConcurrent(1)
            setSeqId(mAtomicInteger.incrementAndGet())
            setTorrentPath(torrentPath)
        }
        val torrentInfo = getTaskInfo(torrentPath)
        val videoFileInfoList = getTaskInfo(torrentPath).mSubFileInfo
            .filter {
                isVideoFile(it.mFileName)
            }.sortedWith(FileNameComparator<TorrentFileInfo>(
                getName = { it.mFileName },
                isDirectory = { false }
            ))
        val (selectedIndexes, deSelectIndexes) = createDownloadIndex(
            selectIndex,
            torrentInfo,
            videoFileInfoList
        )

        stopAllTask()

        //启动下载任务
        val taskId = createTorrentTask(btTaskParam, selectedIndexes, deSelectIndexes)
        if (taskId == INVALID_ID) {
            return Pair(null, videoFileInfoList)
        }

        mTaskList[torrentPath] = taskId
        val fileName = videoFileInfoList[selectIndex].mFileName
        val filePath = "${btTaskParam.mFilePath}/$fileName"
        val playUrl = XLTaskLocalUrl()
        XLDownloadManager.getInstance().getLocalUrl(filePath, playUrl)

        return Pair(playUrl.mStrUrl, videoFileInfoList)
    }

    suspend fun generatePlayUrl(
        torrent: TorrentBean,
        index: Int
    ): String? {
        // 停止其它任务
        stopAllTask()

        //启动下载任务
        val taskId = createPlayTask(torrent, index)
        if (taskId == INVALID_ID) {
            return null
        }

        // 保存任务ID
        mTaskList[torrent.torrentPath] = taskId

        val fileName = torrent.mSubFileInfo.find { it.mFileIndex == index }?.mFileName ?: "temp.mp4"
        val filePath = "${cacheDirectory.absolutePath}/$fileName"
        val playUrl = XLTaskLocalUrl()
        XLDownloadManager.getInstance().getLocalUrl(filePath, playUrl)
        return playUrl.mStrUrl
    }

    fun stopTask(taskId: Long) {
        XLTaskHelper.getInstance().deleteTask(taskId, cacheDirectory.absolutePath)

        mTaskList.entries.find { it.value == taskId }?.let {
            mTaskList.remove(it.key)
        }

    }

    private fun stopAllTask() {
        val iterator = mTaskList.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            XLTaskHelper.getInstance().deleteTask(entity.value, cacheDirectory.absolutePath)
            iterator.remove()
        }
    }

    /**
     * 等待种子下载完成，超时5秒
     */
    private suspend fun waitTorrentDownloaded(
        torrentTaskId: Long,
        param: MagnetTaskParam
    ): String? {
        return withTimeoutOrNull(TIME_OUT_DOWNLOAD_TORRENT) {
            var taskInfo = XLTaskHelper.getInstance().getTaskInfo(torrentTaskId)
            while (taskInfo.mTaskStatus == XLConstant.XLTaskStatus.TASK_IDLE
                || taskInfo.mTaskStatus == XLConstant.XLTaskStatus.TASK_RUNNING
            ) {
                delay(300L)
                taskInfo = XLTaskHelper.getInstance().getTaskInfo(torrentTaskId)
            }
            XLTaskHelper.getInstance().stopTask(torrentTaskId)
            return@withTimeoutOrNull if (
                taskInfo.mTaskStatus == XLConstant.XLTaskStatus.TASK_SUCCESS
            ) {
                "${param.mFilePath}/${param.mFileName}"
            } else {
                null
            }
        }
    }

    private suspend fun createPlayTask(
        torrent: TorrentBean,
        index: Int,
    ): Long {
        val btTaskParam = BtTaskParam().apply {
            setCreateMode(1)
            setFilePath(cacheDirectory.absolutePath)
            setMaxConcurrent(1)
            setSeqId(mAtomicInteger.incrementAndGet())
            setTorrentPath(torrent.torrentPath)
        }

        val deselectedIndexes = torrent.mSubFileInfo
            .filter { it.mFileIndex != index }
            .map { it.mFileIndex }
            .toIntArray()

        val selectedIndexSet = BtIndexSet(1).apply {
            mIndexSet = IntArray(index)
        }
        val deselectIndexSet = BtIndexSet(deselectedIndexes.size).apply {
            mIndexSet = deselectedIndexes
        }

        return createTorrentTask(btTaskParam, selectedIndexSet, deselectIndexSet)
    }

    /**
     * 启动下载任务
     */
    private suspend fun createTorrentTask(
        btTaskParam: BtTaskParam,
        selectedIndexes: BtIndexSet,
        deSelectIndexes: BtIndexSet
    ): Long {
        //启动下载任务
        var taskId = XLTaskHelper.getInstance()
            .startTask(btTaskParam, selectedIndexes, deSelectIndexes)
        if (taskId == INVALID_ID) {
            stopTask(taskId)
            delay(200)
            taskId = XLTaskHelper.getInstance()
                .startTask(btTaskParam, selectedIndexes, deSelectIndexes)
        }

        if (taskId == INVALID_ID) {
            stopTask(taskId)
            return INVALID_ID
        }

        //任务无法下载
        if (checkTaskFailed(taskId)) {
            stopTask(taskId)
            return INVALID_ID
        }

        return taskId
    }

    /**
     * 创建下载索引和忽略下载索引
     */
    private fun createDownloadIndex(
        selectIndex: Int,
        torrentInfo: TorrentInfo,
        videoFileInfoList: List<TorrentFileInfo>
    ): Pair<BtIndexSet, BtIndexSet> {

        val selectFileIndex = videoFileInfoList[selectIndex].mFileIndex

        //已选中索引
        val selectIndexSet = BtIndexSet(1)
        selectIndexSet.mIndexSet[0] = selectFileIndex

        //未选中索引
        val deSelectIndexSet: BtIndexSet
        if (torrentInfo.mFileCount > 1) {
            deSelectIndexSet = BtIndexSet(torrentInfo.mFileCount - 1)
            var deSelectIndex = 0
            torrentInfo.mSubFileInfo.forEach {
                if (it.mFileIndex != selectFileIndex) {
                    deSelectIndexSet.mIndexSet[deSelectIndex] = it.mFileIndex
                    deSelectIndex++
                }
            }
        } else {
            deSelectIndexSet = BtIndexSet(0)
        }

        return Pair(selectIndexSet, deSelectIndexSet)
    }

    /**
     * 检查下载任务是否失败
     */
    private suspend fun checkTaskFailed(taskId: Long): Boolean {
        delay(2000)

        //任务下载失败
        val taskStatus = XLTaskHelper.getInstance().getTaskInfo(taskId).mTaskStatus
        if (taskStatus == XLConstant.XLTaskStatus.TASK_FAILED) {
            return true
        }

        return false
    }
}