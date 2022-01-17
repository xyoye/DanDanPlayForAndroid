package com.xyoye.common_component.utils

import com.xyoye.common_component.config.AppConfig


/**
 * Created by xyoye on 2022/1/17
 */
object FileNameUtils {

    fun fileNeedHide(name: String): Boolean {
        val showHiddenFile = AppConfig.isShowHiddenFile()
        return showHiddenFile.not() && name.startsWith(".")
    }
}