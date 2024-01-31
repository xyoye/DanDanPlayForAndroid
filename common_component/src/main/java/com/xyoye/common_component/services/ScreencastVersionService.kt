package com.xyoye.common_component.services

import com.alibaba.android.arouter.facade.template.IProvider

/**
 * Created by xyoye on 2024/1/31
 */

interface ScreencastVersionService : IProvider {

    fun getVersion(): Int
}