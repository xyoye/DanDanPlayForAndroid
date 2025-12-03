package com.xyoye.storage_component.services

import android.content.Context
import com.therouter.inject.ServiceProvider
import com.xyoye.common_component.services.ScreencastProvideService
import com.xyoye.data_component.entity.MediaLibraryEntity

/**
 * Created by xyoye on 2022/9/15
 */

@ServiceProvider
class ScreencastProvideServiceImpl : ScreencastProvideService {

    override fun isRunning(context: Context): Boolean {
        return com.xyoye.storage_component.services.ScreencastProvideService.isRunning(context)
    }

    override fun startService(context: Context, receiver: MediaLibraryEntity) {
        com.xyoye.storage_component.services.ScreencastProvideService.start(context, receiver)
    }

    override fun stopService(context: Context) {
        com.xyoye.storage_component.services.ScreencastProvideService.stop(context)
    }
}