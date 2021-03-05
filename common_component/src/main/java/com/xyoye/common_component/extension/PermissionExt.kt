package com.xyoye.common_component.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.xyoye.common_component.permission.PermissionManager
import com.xyoye.common_component.permission.PermissionRequest

/**
 * Created by xyoye on 2020/7/29.
 */

/**
 * AppCompatActivity 请求权限的扩展函数
 */
inline fun AppCompatActivity.requestPermissions(
    vararg permissions: String,
    crossinline requestBlock: PermissionRequest.() -> Unit
) {
    val permissionRequest = PermissionRequest().apply(requestBlock)
    requireNotNull(permissionRequest.resultCallback) {
        "No result callback found."
    }
    PermissionManager.requestPermissions(
        this,
        permissionRequest.requestCode,
        permissionRequest.handleRationale,
        permissionRequest.resultCallback!!,
        *permissions
    )
}

/**
 * Fragment 请求权限的扩展函数
 */
inline fun Fragment.requestPermissions(
    vararg permissions: String,
    crossinline requestBlock: PermissionRequest.() -> Unit
) {
    val permissionRequest = PermissionRequest().apply(requestBlock)
    requireNotNull(permissionRequest.resultCallback) {
        "No result callback found."
    }
    PermissionManager.requestPermissions(
        this,
        permissionRequest.requestCode,
        permissionRequest.handleRationale,
        permissionRequest.resultCallback!!,
        *permissions
    )
}