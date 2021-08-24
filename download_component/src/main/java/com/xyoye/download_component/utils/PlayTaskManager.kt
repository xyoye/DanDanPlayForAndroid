package com.xyoye.download_component.utils

import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.ErrorCodeToMsg.ErrCodeToMsg
import com.xunlei.downloadlib.parameter.XLConstant
import com.xyoye.common_component.bridge.PlayTaskBridge
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.PathHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2021/8/8.
 */

object PlayTaskManager {

    private var isInitialed = false

    private var TASK_ERROR_MSG = JsonHelper.parseJsonMap(ErrCodeToMsg)

    private var TASK_STATUS_MSG = mapOf(
        Pair(XLConstant.XLTaskStatus.TASK_FAILED, "Failed"),
        Pair(XLConstant.XLTaskStatus.TASK_IDLE, "Idle"),
        Pair(XLConstant.XLTaskStatus.TASK_RUNNING, "Running"),
        Pair(XLConstant.XLTaskStatus.TASK_STOPPED, "Stopped"),
        Pair(XLConstant.XLTaskStatus.TASK_SUCCESS, "Success")
    )

    fun init() {
        if (isInitialed)
            return

        isInitialed = true

        PlayTaskBridge.taskRemoveLiveData.observeForever {
            onPlayTaskRemove(it)
        }

        PlayTaskBridge.taskInfoQuery = { id ->
            val taskInfo = XLTaskHelper.getInstance().getTaskInfo(id)
            val status = TASK_STATUS_MSG[taskInfo.mTaskStatus] ?: "Unknown_${taskInfo.mTaskStatus}"
            val code = taskInfo.mErrorCode.toString()
            val msg = TASK_ERROR_MSG[code]?.trim() ?: ""
            "\n[$status, 0x$code]\n[$msg]"
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
}