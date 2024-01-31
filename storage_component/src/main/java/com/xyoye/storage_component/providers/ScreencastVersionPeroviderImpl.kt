package com.xyoye.storage_component.providers

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.services.ScreencastVersionService
import com.xyoye.storage_component.utils.screencast.Constant

/**
 * Created by xyoye on 2024/1/31
 */

@Route(path = RouteTable.Stream.ScreencastVersion, name = "投屏版本信息提供者")
class ScreencastVersionProviderImpl : ScreencastVersionService {

    override fun init(context: Context?) {

    }

    override fun getVersion(): Int {
        return Constant.version
    }
}