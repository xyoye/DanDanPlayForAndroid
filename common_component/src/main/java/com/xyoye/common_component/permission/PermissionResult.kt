package com.xyoye.common_component.permission

/**
 * Created by xyoye on 2020/7/29.
 */

sealed class PermissionResult (val requestCode: Int){
    //权限授权成功
    class PermissionGranted(requestCode: Int): PermissionResult(requestCode)

    //权限授权失败
    class PermissionDenied(requestCode: Int, val deniedPermissions: List<DeniedPermission>) : PermissionResult(requestCode)

    //权限第一次授权失败，需要额外处理（如：提示为何需要此权限等）
    class PermissionRationale(requestCode: Int): PermissionResult(requestCode)
}