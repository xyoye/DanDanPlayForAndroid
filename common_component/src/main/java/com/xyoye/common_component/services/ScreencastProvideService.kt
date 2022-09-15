package com.xyoye.common_component.services

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider
import com.xyoye.data_component.entity.MediaLibraryEntity

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/15
 *     desc  :
 * </pre>
 */

interface ScreencastProvideService : IProvider {

    fun startService(context: Context, receiver: MediaLibraryEntity)

    fun stopService(context: Context)
}