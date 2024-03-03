package com.xyoye.storage_component.ui.dialog

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.DialogAlistLoginBinding
import com.xyoye.storage_component.ui.activities.storage_plus.StoragePlusActivity

/**
 * Created by xyoye on 2021/1/26.
 */

class AlistStorageEditDialog(
    private val activity: StoragePlusActivity,
    private val library: MediaLibraryEntity?
) : StorageEditDialog<DialogAlistLoginBinding>(activity) {

    private lateinit var binding: DialogAlistLoginBinding

    override fun getChildLayoutId() = R.layout.dialog_alist_login

    override fun initView(binding: DialogAlistLoginBinding) {
        this.binding = binding
        val isEditMode = library != null

        setTitle(if (isEditMode) "编辑Alist帐号" else "添加Alist帐号")

        val editLibrary = library ?: MediaLibraryEntity(
            0,
            "",
            "",
            MediaType.ALSIT_STORAGE
        )
        binding.library = editLibrary

        binding.serverTestConnectTv.setOnClickListener {
            if (checkParams(editLibrary)) {
                activity.testStorage(editLibrary)
            }
        }

        binding.passwordToggleIv.setOnClickListener {
            if (binding.passwordToggleIv.isSelected) {
                binding.passwordToggleIv.isSelected = false
                binding.passwordEt.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            } else {
                binding.passwordToggleIv.isSelected = true
                binding.passwordEt.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
        }

        setPositiveListener {
            if (checkParams(editLibrary)) {
                if (editLibrary.displayName.isEmpty()) {
                    editLibrary.displayName = "Alist媒体库"
                }
                editLibrary.describe = editLibrary.url
                activity.addStorage(editLibrary)
            }
        }

        setNegativeListener {
            activity.finish()
        }
    }

    override fun onTestResult(result: Boolean) {
        if (result) {
            binding.serverStatusTv.text = "连接成功"
            binding.serverStatusTv.setTextColorRes(R.color.text_blue)
        } else {
            binding.serverStatusTv.text = "连接失败"
            binding.serverStatusTv.setTextColorRes(R.color.text_red)
        }
    }

    private fun checkParams(serverData: MediaLibraryEntity): Boolean {
        if (serverData.url.isEmpty()) {
            ToastCenter.showWarning("请填写服务器地址")
            return false
        }
        if (!serverData.url.endsWith("/")) {
            serverData.url = "${serverData.url}/"
        }

        val serverUrl = serverData.url
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            ToastCenter.showWarning("请填写服务器协议：http或https")
            return false
        }
        if (serverData.account.isNullOrEmpty()) {
            ToastCenter.showWarning("请填写帐号")
            return false
        }
        if (serverData.password.isNullOrEmpty()) {
            ToastCenter.showWarning("请填写密码")
            return false
        }
        return true
    }
}