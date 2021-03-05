package com.xyoye.common_component.permission

/**
 * Created by xyoye on 2020/7/30.
 */

data class DeniedPermission(
    val permission: String,
    val isDeniedPermanently: Boolean = false
)