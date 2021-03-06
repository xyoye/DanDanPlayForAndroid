package com.xyoye.stream_component.ui.dialog

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.DialogFtpLoginBinding
import java.nio.charset.Charset

/**
 * Created by xyoye on 2021/1/28.
 */

class FTPLoginDialog : BaseBottomDialog<DialogFtpLoginBinding> {
    private var originalStorage: MediaLibraryEntity? = null
    private lateinit var addMediaStorage: (MediaLibraryEntity) -> Unit
    private lateinit var testConnect: (MediaLibraryEntity) -> Unit
    private lateinit var testConnectResult: MutableLiveData<Boolean>

    constructor() : super()

    constructor(
        originalStorage: MediaLibraryEntity?,
        addMediaStorage: (MediaLibraryEntity) -> Unit,
        testConnect: (MediaLibraryEntity) -> Unit,
        testConnectResult: MutableLiveData<Boolean>
    ) : super(true) {
        this.originalStorage = originalStorage
        this.addMediaStorage = addMediaStorage
        this.testConnect = testConnect
        this.testConnectResult = testConnectResult
    }

    override fun getChildLayoutId() = R.layout.dialog_ftp_login

    override fun initView(binding: DialogFtpLoginBinding) {
        val isEditStorage = originalStorage != null

        setTitle(if (isEditStorage) "编辑FTP帐号" else "添加FTP帐号")

        val serverData = originalStorage ?: MediaLibraryEntity(
            0,
            "",
            "",
            MediaType.FTP_SERVER,
            null,
            null,
            false,
            21
        )
        binding.serverData = serverData

        //编辑模式下，选中匿名
        if (isEditStorage && originalStorage!!.isAnonymous) {
            binding.loginModeRg.check(R.id.anonymous_rb)
            binding.accountEt.isVisible = false
            binding.accountEt.setText("")
            binding.passwordEt.isVisible = false
            binding.passwordEt.setText("")
        }

        binding.serverTestConnectIv.setOnClickListener {
            if (checkParams(serverData)) {
                testConnect.invoke(serverData)
            }
        }

        testConnectResult.observe(mOwnerActivity!!, {
            if (it) {
                binding.serverStatusTv.text = "连接成功"
                binding.serverStatusTv.setTextColorRes(R.color.text_blue)
            } else {
                binding.serverStatusTv.text = "连接失败"
                binding.serverStatusTv.setTextColorRes(R.color.text_red)
            }
        })

        binding.loginModeRg.setOnCheckedChangeListener { _, checkedId ->
            val isAnonymous = checkedId == R.id.anonymous_rb
            binding.accountEt.isGone = isAnonymous
            binding.passwordEt.isGone = isAnonymous
            if (isAnonymous) {
                binding.accountEt.setText("")
                binding.passwordEt.setText("")
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
            if (checkParams(serverData)) {
                addMediaStorage.invoke(serverData)
                dismiss()
                mOwnerActivity?.finish()
            }
        }

        setNegativeListener {
            dismiss()
            mOwnerActivity?.finish()
        }
    }

    private fun checkParams(serverData: MediaLibraryEntity): Boolean {
        if (serverData.ftpAddress.isEmpty()) {
            ToastCenter.showWarning("请填写FTP地址")
            return false
        }

        if (serverData.ftpEncoding.isEmpty()) {
            ToastCenter.showWarning("请填写编码格式")
            return false
        }

        if (!Charset.isSupported(serverData.ftpEncoding)) {
            ToastCenter.showWarning("不支持的编码格式")
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
}