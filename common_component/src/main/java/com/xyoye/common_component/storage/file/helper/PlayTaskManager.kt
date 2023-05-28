package com.xyoye.common_component.storage.file.helper

import com.xunlei.downloadlib.parameter.ErrorCodeToMsg.ErrCodeToMsg
import com.xunlei.downloadlib.parameter.XLConstant
import com.xyoye.common_component.bridge.PlayTaskBridge
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.common_component.utils.thunder.ThunderManager
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

        SupervisorScope.Main.launch {
            PlayTaskBridge.taskRemoveLiveData.observeForever {
                onPlayTaskRemove(it)
            }
        }

        PlayTaskBridge.taskInfoQuery = { id ->
            val taskInfo = ThunderManager.getInstance().getTaskInfo(id)
            val status = TASK_STATUS_MSG[taskInfo.mTaskStatus] ?: "Unknown_${taskInfo.mTaskStatus}"
            val code = taskInfo.mErrorCode.toString()
            val msg = TASK_ERROR_MSG[code]?.trim() ?: ""
            "\n[$status, 0x$code]\n[$msg]"
        }
    }

    private fun onPlayTaskRemove(taskId: Long) {
        SupervisorScope.IO.launch {
            ThunderManager.getInstance().stopTask(taskId)
        }
    }
}