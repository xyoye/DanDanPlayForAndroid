package com.xyoye.common_component.permission

/**
 * Created by xyoye on 2020/7/29.
 */

class PermissionRequest(
    //请求码
    var requestCode: Int = 1001,
    //是否处理 用户第一次拒绝请求 的情况
    var handleRationale: Boolean = false,
    //请求结果回调
    var resultCallback: (PermissionResult.() -> Unit)? = null
)