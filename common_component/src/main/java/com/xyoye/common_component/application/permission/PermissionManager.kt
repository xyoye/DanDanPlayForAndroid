package com.xyoye.common_component.application.permission

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Created by xyoye on 2020/7/29.
 */

class PermissionManager : Fragment() {

    companion object {
        private const val FRAGMENT_TAG = "tag_fragment_permission_manager"
        private const val KEY_PERMISSION_LAUNCHER = "key_permission_launcher"

        fun requestPermissions(
            fragmentManager: FragmentManager,
            permissions: Array<String>,
            permissionResult: PermissionResult
        ) {
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.findFragmentByTag(FRAGMENT_TAG)?.let {
                transaction.remove(it)
            }
            PermissionManager().apply {
                this.permissionResult = permissionResult
                transaction.add(this, FRAGMENT_TAG)
                transaction.commitNow()
            }.requestPermissions(permissions)
        }
    }

    private var permissionResult: PermissionResult? = null
    private lateinit var permissionObserver: PermissionLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionLifecycleObserver(requireActivity().activityResultRegistry, this).also {
            permissionObserver = it
            lifecycle.addObserver(it)
        }
    }

    override fun onDestroy() {
        lifecycle.removeObserver(permissionObserver)
        super.onDestroy()
    }

    fun requestPermissions(permissions: Array<String>) {
        permissionObserver.requestPermissions(permissions)
    }

    private fun onPermissionGranted() {
        permissionResult?.invokeGranted()
    }

    private fun onPermissionDenied(deniedPermissions: List<String>) {
        permissionResult?.invokeDenied(deniedPermissions)
    }

    private fun onRequestComplete() {
        try {
            parentFragmentManager.beginTransaction().remove(this).commitNow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class PermissionLifecycleObserver(
        private val registry: ActivityResultRegistry,
        private val manager: PermissionManager
    ) : DefaultLifecycleObserver {
        private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>

        //ActivityResultRegistry会在ON_DESTROY后，将请求结果保存在PendingResult中，并在下次ON_START时回调
        //但在此权限请求工具中，每次请求都会新建Fragment，所以不应在ON_START接收上一次的结果
        private var mRequestedPermission = false

        override fun onCreate(lifecycleOwner: LifecycleOwner) {
            requestPermissions = registry.register(
                KEY_PERMISSION_LAUNCHER,
                lifecycleOwner,
                ActivityResultContracts.RequestMultiplePermissions()
            ) { grantState ->
                if (mRequestedPermission.not()) {
                    return@register
                }
                val deniedPermissions = grantState.filter { it.value.not() }.map { it.key }
                if (deniedPermissions.isEmpty()) {
                    //不存在未授权的权限
                    manager.onPermissionGranted()
                } else {
                    //存在未授权的权限
                    manager.onPermissionDenied(deniedPermissions)
                }
                manager.onRequestComplete()
            }


        }

        override fun onDestroy(owner: LifecycleOwner) {
            mRequestedPermission = false
        }

        fun requestPermissions(permissions: Array<String>) {
            mRequestedPermission = true
            requestPermissions.launch(permissions)
        }
    }
}