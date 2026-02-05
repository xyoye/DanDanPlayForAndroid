package com.xyoye.common_component.application.permission

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Created by xyoye on 2022/12/27
 */

class Permission {
    /**
     * 存储权限
     */
    val storage = StoragePermissionRequest()

    /**
     * 相机权限
     */
    val camera = PermissionRequest(
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE
        )
    )

    class PermissionRequest(
        private val permissions: Array<String>
    ) {

        fun request(fragment: Fragment, result: PermissionResult.() -> Unit) {
            requestPermission(fragment.childFragmentManager, result)
        }

        fun request(activity: AppCompatActivity, result: PermissionResult.() -> Unit) {
            requestPermission(activity.supportFragmentManager, result)
        }

        private fun requestPermission(
            fragmentManager: FragmentManager,
            result: PermissionResult.() -> Unit
        ) {
            PermissionManager.requestPermissions(
                fragmentManager,
                permissions,
                PermissionResult().apply(result)
            )
        }
    }

    class StoragePermissionRequest {

        fun request(fragment: Fragment, result: PermissionResult.() -> Unit) {
            requestStorage(fragment.childFragmentManager, result)
        }

        fun request(activity: AppCompatActivity, result: PermissionResult.() -> Unit) {
            requestStorage(activity.supportFragmentManager, result)
        }

        private fun requestStorage(
            fragmentManager: FragmentManager,
            result: PermissionResult.() -> Unit
        ) {
            PermissionManager.requestStorage(
                fragmentManager,
                PermissionResult().apply(result)
            )
        }
    }
}
