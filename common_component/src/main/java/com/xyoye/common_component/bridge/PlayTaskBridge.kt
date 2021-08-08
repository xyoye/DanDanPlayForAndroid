package com.xyoye.common_component.bridge

import androidx.lifecycle.MutableLiveData

/**
 * Created by xyoye on 2021/8/8.
 */

object PlayTaskBridge {

    val taskRemoveLiveData = MutableLiveData<Long>()
    val taskRefreshLiveData = MutableLiveData<Long>()

    fun sendTaskRemoveMsg(taskId: Long?) {
        if (taskId == -1L)
            return
        taskRemoveLiveData.postValue(taskId)
    }

    fun sendTaskRefreshMsg(taskId: Long?) {
        if (taskId == -1L)
            return
        taskRefreshLiveData.postValue(taskId)
    }
}