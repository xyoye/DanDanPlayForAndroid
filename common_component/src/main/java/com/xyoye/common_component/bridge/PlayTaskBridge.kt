package com.xyoye.common_component.bridge

import androidx.lifecycle.MutableLiveData

/**
 * Created by xyoye on 2021/8/8.
 */

object PlayTaskBridge {

    val taskRemoveLiveData = MutableLiveData<Long>()
    var taskInfoQuery: ((id: Long) -> String)? = null

    fun sendTaskRemoveMsg(taskId: Long) {
        if (taskId == -1L)
            return
        taskRemoveLiveData.postValue(taskId)
    }

    fun getTaskLog(id: Long): String {
        return taskInfoQuery?.invoke(id) ?: ""
    }
}