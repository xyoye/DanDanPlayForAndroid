package com.xyoye.common_component.permission

/**
 * Created by xyoye on 2020/7/29.
 */

open class PermissionResult {

    private var permissionGranted: ((requestCode: Int) -> Unit)? = null
    private var permissionDenied: ((requestCode: Int, deniedPermissions: List<DeniedPermission>) -> Unit)? =
        null
    private var permissionRationale: ((requestCode: Int) -> Unit)? = null

    /**
     * 授权成功
     */
    fun onGranted(permissionGranted: ((requestCode: Int) -> Unit)) {
        this.permissionGranted = permissionGranted
    }
    /**
     * 授权失败
     */
    fun onDenied(permissionDenied: ((requestCode: Int, deniedPermissions: List<DeniedPermission>) -> Unit)) {
        this.permissionDenied = permissionDenied
    }

    /**
     * 权限第一次授权失败，需要额外处理（如：提示为何需要此权限等）
     */
    fun onRationale(permissionRationale: ((requestCode: Int) -> Unit)) {
        this.permissionRationale = permissionRationale
    }

    /**
     * 是否处理第一次授权失败
     */
    fun isRationale() = permissionRationale != null

    fun invokeGranted(requestCode: Int) {
        permissionGranted?.invoke(requestCode)
    }

    fun invokeDenied(requestCode: Int, deniedPermissions: List<DeniedPermission>) {
        permissionDenied?.invoke(requestCode, deniedPermissions)
    }

    fun invokeRationale(requestCode: Int) {
        permissionRationale?.invoke(requestCode)
    }
}