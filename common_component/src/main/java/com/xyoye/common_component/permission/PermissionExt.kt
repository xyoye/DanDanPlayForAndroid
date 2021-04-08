package com.xyoye.common_component.permission

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Created by xyoye on 2020/7/29.
 */

/**
 * AppCompatActivity 请求权限的扩展函数
 */
inline fun AppCompatActivity.requestPermissions(
    vararg permissions: String,
    requestCode: Int = 1001,
    crossinline permissionResult: PermissionResult.() -> Unit,
) {
    PermissionManager.requestPermissions(
        this,
        requestCode,
        PermissionResult().apply(permissionResult),
        *permissions
    )
}

/**
 * Fragment 请求权限的扩展函数
 */
inline fun Fragment.requestPermissions(
    vararg permissions: String,
    requestCode: Int = 1001,
    crossinline permissionResult: PermissionResult.() -> Unit,
) {
    PermissionManager.requestPermissions(
        this,
        requestCode,
        PermissionResult().apply(permissionResult),
        *permissions
    )
}