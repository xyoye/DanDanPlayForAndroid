package com.xyoye.common_component.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Created by xyoye on 2022/12/25.
 */

object SupervisorScope {
    val IO =  CoroutineScope(SupervisorJob() + Dispatchers.IO)
}