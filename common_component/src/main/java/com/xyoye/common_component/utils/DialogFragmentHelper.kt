package com.xyoye.common_component.utils

import android.os.Bundle

/**
 * Created by xyoye on 2021/3/5.
 *
 * 用于防止DialogFragment被重建后，构建方法中值为空的情况
 * 如果被重建，自动关闭DialogFragment
 */

object DialogFragmentHelper {

    private const val KEY_REBUILD_STATE = "key_rebuild_state"

    fun buildArgument() = Bundle().apply {
        //构造方法中，设置值为true
        putBoolean(KEY_REBUILD_STATE, true)
    }

    fun isArgumentInvalid(argument: Bundle?): Boolean {
        //获取不到argument，无效
        if (argument == null)
            return true

        val isValid = argument.getBoolean(KEY_REBUILD_STATE, false)
        //获取到值有效，代表是正常第一次构建的fragment，此时设置值为false
        //这样如果是fragment被系统重建，取得的值就永远是false
        if (isValid) {
            argument.putBoolean(KEY_REBUILD_STATE, false)
            return false
        }
        return true
    }
}