package com.xyoye.local_component.ui.dialog

import android.content.Intent
import android.net.Uri
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogShooterSecretBinding

/**
 * Created by xyoye on 2021/2/24.
 */

class ShooterSecretDialog : BaseBottomDialog<DialogShooterSecretBinding>() {

    override fun getChildLayoutId() = R.layout.dialog_shooter_secret

    override fun initView(binding: DialogShooterSecretBinding) {

        setTitle("完善API密钥")

        setNegativeListener { dismiss() }

        setPositiveListener {
            val secret = binding.shooterSecretEt.text.toString()
            if (secret.isEmpty()) {
                ToastCenter.showError("API密钥不能为空")
                return@setPositiveListener
            }

            SubtitleConfig.putShooterSecret(secret)
            dismiss()
        }

        binding.loginShooterTv.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://secure.assrt.net/user/logon.xml")
            startActivity(intent)
        }
    }
}