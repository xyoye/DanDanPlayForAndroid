package com.xyoye.storage_component.services

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.services.ScreencastProvideService
import com.xyoye.data_component.entity.MediaLibraryEntity

/**
 * Created by xyoye on 2022/9/15
 */

@Route(path = RouteTable.Stream.ScreencastProvide, name = "投屏内容提供服务")
class ScreencastProvideServiceImpl : ScreencastProvideService {

    override fun init(context: Context?) {

    }

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