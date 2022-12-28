package com.xyoye.common_component.application.permission

/**
 * Created by xyoye on 2020/7/29.
 */

class PermissionResult {

    private var permissionGranted: (() -> Unit)? = null
    private var permissionDenied: ((List<String>) -> Unit)? =
        null

    /**
     * 授权成功
     */
    fun onGranted(permissionGranted: (() -> Unit)) {
        this.permissionGranted = permissionGranted
    }

    /**
     * 授权失败
     */
    fun onDenied(permissionDenied: ((deniedPermissions: List<String>) -> Unit)) {
        this.permissionDenied = permissionDenied
    }

    fun invokeGranted() {
        permissionGranted?.invoke()
    }

    fun invokeDenied(deniedPermissions: List<String>) {
        permissionDenied?.invoke(deniedPermissions)
    }
}