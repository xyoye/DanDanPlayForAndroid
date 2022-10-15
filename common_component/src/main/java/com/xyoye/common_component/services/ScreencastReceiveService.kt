package com.xyoye.common_component.services

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/15
 *     desc  :
 * </pre>
 */

interface ScreencastReceiveService : IProvider {

    fun isRunning(context: Context): Boolean

    fun stopService(context: Context)

    fun startService(context: Context, port: Int, password: String?)
}