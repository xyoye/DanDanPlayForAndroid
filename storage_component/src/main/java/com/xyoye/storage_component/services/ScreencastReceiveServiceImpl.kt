package com.xyoye.storage_component.services

import android.content.Context
import com.therouter.inject.ServiceProvider
import com.xyoye.common_component.services.ScreencastReceiveService

/**
 * Created by xyoye on 2022/9/15
 */

@ServiceProvider
class ScreencastReceiveServiceImpl : ScreencastReceiveService {

    override fun isRunning(context: Context): Boolean {
        return com.xyoye.storage_component.services.ScreencastReceiveService.isRunning(context)
    }

    override fun stopService(context: Context) {
        com.xyoye.storage_component.services.ScreencastReceiveService.stop(context)
    }

    override fun startService(context: Context, port: Int, password: String?) {
        com.xyoye.storage_component.services.ScreencastReceiveService.start(context, port, password)
    }
}