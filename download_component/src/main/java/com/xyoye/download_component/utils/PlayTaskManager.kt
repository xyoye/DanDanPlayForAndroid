package com.xyoye.download_component.utils

import com.xunlei.downloadlib.XLDownloadManager
import com.xunlei.downloadlib.XLTaskHelper
import com.xyoye.common_component.bridge.PlayTaskBridge
import com.xyoye.common_component.utils.PathHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2021/8/8.
 */

object PlayTaskManager {

    private var isInitialed = false

    fun init() {
        if (isInitialed)
            return

        isInitialed = true

        PlayTaskBridge.taskRemoveLiveData.observeForever {
            onPlayTaskRemove(it)
        }

        PlayTaskBridge.taskRefreshLiveData.observeForever {
            onPlayTaskRefresh(it)
        }
    }

    private fun onPlayTaskRemove(taskId: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            val playCacheDir = PathHelper.getPlayCacheDirectory()
            XLTaskHelper.getInstance().stopTask(taskId)
            XLTaskHelper.getInstance().deleteTask(taskId, playCacheDir.absolutePath)
            playCacheDir.delete()
        }

    }

    private fun onPlayTaskRefresh(taskId: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            XLDownloadManager.getInstance().stopTask(taskId)
            delay(1000L)
            XLDownloadManager.getInstance().startTask(taskId)
        }
    }
}