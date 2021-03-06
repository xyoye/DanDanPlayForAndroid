package com.xyoye.user_component.ui.dialog

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.DialogUpdatePasswordBinding

/**
 * Created by xyoye on 2021/1/11.
 */

class UpdatePasswordDialog : BaseBottomDialog<DialogUpdatePasswordBinding> {
    private lateinit var callback: (old: String, new: String) -> Boolean

    constructor() : super()
    constructor(
        callback: (old: String, new: String) -> Boolean
    ) : super(true) {
        this.callback = callback
    }

    private lateinit var binding: DialogUpdatePasswordBinding

    override fun getChildLayoutId() = R.layout.dialog_update_password

    override fun initView(binding: DialogUpdatePasswordBinding) {
        this.binding = binding

        setTitle("修改密码")

        setNegativeListener { dismiss() }

        setPositiveListener {
            val oldPassword = binding.oldPasswordEt.text.toString()
            val newPassword = binding.newPasswordEt.text.toString()
            if (oldPassword.isEmpty()) {
                ToastCenter.showWarning("旧密码不能为空")
                return@setPositiveListener
            }
            if (newPassword.isEmpty()) {
                ToastCenter.showWarning("新密码不能为空")
                return@setPositiveListener
            }
            if (oldPassword.length < 5 || newPassword.length < 5) {
                ToastCenter.showWarning("密码长度应为5-20")
                return@setPositiveListener
            }

            if (callback.invoke(oldPassword, newPassword)) {
                dismiss()
            }
        }

        binding.newPasswordVisibleIv.isSelected = false
        binding.newPasswordVisibleIv.setOnClickListener {
            if (binding.newPasswordVisibleIv.isSelected) {
                binding.newPasswordVisibleIv.isSelected = false
                binding.newPasswordEt.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            } else {
                binding.newPasswordVisibleIv.isSelected = true
                binding.newPasswordEt.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
        }

        binding.oldPasswordEt.postDelayed({ showKeyboard(binding.oldPasswordEt) }, 200)
    }

    override fun dismiss() {
        hideKeyboard(binding.oldPasswordEt)
        hideKeyboard(binding.newPasswordEt)
        super.dismiss()
    }
}