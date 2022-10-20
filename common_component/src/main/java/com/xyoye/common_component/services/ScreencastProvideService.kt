package com.xyoye.common_component.services

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider
import com.xyoye.data_component.entity.MediaLibraryEntity

/**
 * Created by xyoye on 2022/9/15
 */

interface ScreencastProvideService : IProvider {

    fun isRunning(context: Context): Boolean

    fun startService(context: Context, receiver: MediaLibraryEntity)

    fun stopService(context: Context)
}