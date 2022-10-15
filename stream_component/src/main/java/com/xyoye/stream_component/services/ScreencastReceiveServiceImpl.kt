package com.xyoye.stream_component.services

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.services.ScreencastReceiveService

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/15
 *     desc  :
 * </pre>
 */

@Route(path = RouteTable.Stream.ScreencastReceive, name = "投屏内容接收服务")
class ScreencastReceiveServiceImpl : ScreencastReceiveService {

    override fun init(context: Context?) {

    }

    override fun isRunning(context: Context): Boolean {
        return com.xyoye.stream_component.services.ScreencastReceiveService.isRunning(context)
    }

    override fun stopService(context: Context) {
        com.xyoye.stream_component.services.ScreencastReceiveService.stop(context)
    }

    override fun startService(context: Context, port: Int, password: String?) {
        com.xyoye.stream_component.services.ScreencastReceiveService.start(context, port, password)
    }
}