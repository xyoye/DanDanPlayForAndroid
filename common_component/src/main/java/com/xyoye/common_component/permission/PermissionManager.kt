package com.xyoye.common_component.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Created by xyoye on 2020/7/29.
 */

class PermissionManager : Fragment() {

    companion object {
        private const val FRAGMENT_TAG = "tag_fragment_permission_manager"

        fun requestPermissions(
            activityOrFragment: Any,
            requestCode: Int,
            handleRationale: Boolean,
            callback: PermissionResult.() -> Unit,
            vararg permissions: String
        ) {
            //获取 fragment manager
            val fragmentManager = if (activityOrFragment is AppCompatActivity) {
                activityOrFragment.supportFragmentManager
            } else {
                (activityOrFragment as Fragment).childFragmentManager
            }
            //根据TAG寻找fragment
            val fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
            if (fragment != null) {
                //将请求码与回调加入map，并请求权限
                (fragment as PermissionManager).also {
                    it.callbackMap[requestCode] = callback
                }.considerRequestPermission(
                    requestCode,
                    handleRationale,
                    *permissions
                )
            } else {
                //创建fragment，将请求码与回调加入map，并请求权限
                PermissionManager().run {
                    fragmentManager.beginTransaction().add(this, FRAGMENT_TAG).commitNow()
                    callbackMap[requestCode] = callback
                    considerRequestPermission(
                        requestCode,
                        handleRationale,
                        *permissions
                    )
                }
            }
        }
    }

    private val callbackMap = mutableMapOf<Int, PermissionResult.() -> Unit>()
    private val rationalRequest = mutableMapOf<Int, Boolean>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbackMap.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onDestroy() {
        super.onDestroy()
        callbackMap.clear()
    }

    /**
     * 考虑请求权限
     */
    fun considerRequestPermission(
        requestCode: Int,
        handleRationale: Boolean,
        vararg permissions: String
    ) {
        //处理 第一次拒绝的情况
        if (handleRationale) {
            rationalRequest[requestCode]?.let {
                requestPermissions(permissions, requestCode)
                rationalRequest.remove(requestCode)
                return
            }
        }

        //获取请求权限中，未授权的权限
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                it
            ) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        when {
            //未授权为空，则权限请求成功
            notGranted.isEmpty() ->
                onPermissionResult(PermissionResult.PermissionGranted(requestCode))
            //处理 第一次拒绝的情况，返回
            handleRationale and notGranted.any { shouldShowRequestPermissionRationale(it) } -> {
                rationalRequest[requestCode] = true
                onPermissionResult(PermissionResult.PermissionRationale(requestCode))
            }
            //正式请求权限
            else -> {
                requestPermissions(notGranted, requestCode)
            }
        }
    }

    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            //所有权限请求成功
            onPermissionResult(PermissionResult.PermissionGranted(requestCode))
        } else {
            //存在拒绝的权限
            val deniedPermissions = mutableListOf<DeniedPermission>()
            grantResults.forEachIndexed { index, grantResult ->
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    deniedPermissions.add(
                        DeniedPermission(
                            permissions[index],
                            //是否为永久拒绝
                            shouldShowRequestPermissionRationale(permissions[index])
                        )
                    )
                }
            }
            onPermissionResult(
                PermissionResult.PermissionDenied(
                    requestCode,
                    deniedPermissions
                )
            )
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onPermissionResult(permissionResult: PermissionResult) {
        callbackMap[permissionResult.requestCode]?.let {
            permissionResult.it()
        }
        callbackMap.remove(permissionResult.requestCode)
    }
}