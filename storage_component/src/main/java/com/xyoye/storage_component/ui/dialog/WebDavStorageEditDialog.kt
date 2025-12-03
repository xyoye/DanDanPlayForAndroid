package com.xyoye.storage_component.ui.dialog

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.view.isGone
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.DialogWebDavLoginBinding
import com.xyoye.storage_component.ui.activities.storage_plus.StoragePlusActivity

/**
 * Created by xyoye on 2021/1/26.
 */

class WebDavStorageEditDialog(
    private val activity: StoragePlusActivity,
    private val originalStorage: MediaLibraryEntity?
) : StorageEditDialog<DialogWebDavLoginBinding>(activity) {

    private lateinit var binding: DialogWebDavLoginBinding

    override fun getChildLayoutId() = R.layout.dialog_web_dav_login

    override fun initView(binding: DialogWebDavLoginBinding) {
        this.binding = binding
        val isEditStorage = originalStorage != null

        setTitle(if (isEditStorage) "编辑WebDav帐号" else "添加WebDav帐号")

        val serverData = originalStorage ?: MediaLibraryEntity(
            0,
            "",
            "",
            MediaType.WEBDAV_SERVER
        )
        setAnonymous(serverData.isAnonymous)
        setParseMode(serverData.webDavStrict)
        binding.serverData = serverData

        binding.serverTestConnectTv.setOnClickListener {
            if (checkParams(serverData)) {
                activity.testStorage(serverData)
            }
        }

        binding.strictParseTv.setOnClickListener {
            serverData.webDavStrict = true
            setParseMode(true)
        }

        binding.normalParseTv.setOnClickListener {
            serverData.webDavStrict = false
            setParseMode(false)
        }

        binding.anonymousTv.setOnClickListener {
            serverData.isAnonymous = true
            setAnonymous(true)
        }

        binding.accountTv.setOnClickListener {
            serverData.isAnonymous = false
            setAnonymous(false)
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
            if (checkParams(serverData)) {
                if (serverData.displayName.isEmpty()) {
                    serverData.displayName = "WebDav媒体库"
                }
                serverData.describe = serverData.url
                activity.addStorage(serverData)
            }
        }

        setNegativeListener {
            activity.finish()
        }
    }

    override fun onTestResult(result: Boolean) {
        if (result) {
            binding.serverStatusTv.text = "连接成功"
            binding.serverStatusTv.setTextColorRes(com.xyoye.common_component.R.color.text_blue)
        } else {
            binding.serverStatusTv.text = "连接失败"
            binding.serverStatusTv.setTextColorRes(com.xyoye.common_component.R.color.text_red)
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
        if (!serverData.isAnonymous) {
            if (serverData.account.isNullOrEmpty()) {
                ToastCenter.showWarning("请填写帐号")
                return false
            }
            if (serverData.password.isNullOrEmpty()) {
                ToastCenter.showWarning("请填写密码")
                return false
            }
        }
        return true
    }

    private fun setAnonymous(isAnonymous: Boolean) {
        binding.anonymousTv.isSelected = isAnonymous
        binding.anonymousTv.setTextColorRes(
            if (isAnonymous) com.xyoye.common_component.R.color.text_white else com.xyoye.common_component.R.color.text_black
        )

        binding.accountTv.isSelected = !isAnonymous
        binding.accountTv.setTextColorRes(
            if (!isAnonymous) com.xyoye.common_component.R.color.text_white else com.xyoye.common_component.R.color.text_black
        )

        binding.accountEt.isGone = isAnonymous
        binding.passwordEt.isGone = isAnonymous
        binding.passwordFl.isGone = isAnonymous

        if (isAnonymous) {
            binding.accountEt.setText("")
            binding.passwordEt.setText("")
        }
    }

    private fun setParseMode(isStrict: Boolean) {
        binding.strictParseTv.isSelected = isStrict
        binding.strictParseTv.setTextColorRes(
            if (isStrict) com.xyoye.common_component.R.color.text_white else com.xyoye.common_component.R.color.text_black
        )

        binding.normalParseTv.isSelected = isStrict.not()
        binding.normalParseTv.setTextColorRes(
            if (isStrict.not()) com.xyoye.common_component.R.color.text_white else com.xyoye.common_component.R.color.text_black
        )
    }
}