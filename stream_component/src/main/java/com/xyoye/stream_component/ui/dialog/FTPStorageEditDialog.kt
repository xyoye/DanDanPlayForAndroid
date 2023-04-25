package com.xyoye.stream_component.ui.dialog

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.view.isGone
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.DialogFtpLoginBinding
import com.xyoye.stream_component.ui.activities.storage_plus.StoragePlusActivity
import java.nio.charset.Charset

/**
 * Created by xyoye on 2021/1/28.
 */

class FTPStorageEditDialog(
    private val activity: StoragePlusActivity,
    private val originalStorage: MediaLibraryEntity?
) : StorageEditDialog<DialogFtpLoginBinding>(activity) {

    private lateinit var binding: DialogFtpLoginBinding

    override fun getChildLayoutId() = R.layout.dialog_ftp_login

    override fun initView(binding: DialogFtpLoginBinding) {
        this.binding = binding
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
            setAnonymous(true)
        } else {
            setAnonymous(serverData.isAnonymous)
        }
        setActive(serverData.isActiveFTP)

        binding.serverTestConnectTv.setOnClickListener {
            if (checkParams(serverData)) {
                activity.testStorage(serverData)
            }
        }

        binding.anonymousTv.setOnClickListener {
            serverData.isAnonymous = true
            setAnonymous(true)
        }

        binding.accountTv.setOnClickListener {
            serverData.isAnonymous = false
            setAnonymous(false)
        }

        binding.activeTv.setOnClickListener {
            serverData.isActiveFTP = true
            setActive(true)
        }

        binding.passiveTv.setOnClickListener {
            serverData.isActiveFTP = false
            setActive(false)
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
                    serverData.displayName = "FTP媒体库"
                }
                serverData.url = if (serverData.ftpAddress.contains("//"))
                    "${serverData.ftpAddress}:${serverData.port}"
                else
                    "ftp://${serverData.ftpAddress}:${serverData.port}"

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
            binding.serverStatusTv.setTextColorRes(R.color.text_blue)
        } else {
            binding.serverStatusTv.text = "连接失败"
            binding.serverStatusTv.setTextColorRes(R.color.text_red)
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

    private fun setAnonymous(isAnonymous: Boolean) {
        binding.anonymousTv.isSelected = isAnonymous
        binding.anonymousTv.setTextColorRes(
            if (isAnonymous) R.color.text_white else R.color.text_black
        )

        binding.accountTv.isSelected = !isAnonymous
        binding.accountTv.setTextColorRes(
            if (!isAnonymous) R.color.text_white else R.color.text_black
        )

        binding.accountEt.isGone = isAnonymous
        binding.passwordEt.isGone = isAnonymous
        binding.passwordFl.isGone = isAnonymous

        if (isAnonymous) {
            binding.accountEt.setText("")
            binding.passwordEt.setText("")
        }
    }

    private fun setActive(isActive: Boolean) {
        binding.activeTv.isSelected = isActive
        binding.activeTv.setTextColorRes(
            if (isActive) R.color.text_white else R.color.text_black
        )

        binding.passiveTv.isSelected = !isActive
        binding.passiveTv.setTextColorRes(
            if (!isActive) R.color.text_white else R.color.text_black
        )
    }
}