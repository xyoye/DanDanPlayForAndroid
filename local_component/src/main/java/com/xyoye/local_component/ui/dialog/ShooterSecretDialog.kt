package com.xyoye.local_component.ui.dialog

import android.app.Activity
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.startUrlActivity
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogShooterSecretBinding

/**
 * Created by xyoye on 2021/2/24.
 */

class ShooterSecretDialog(
    private val activity: Activity,
) : BaseBottomDialog<DialogShooterSecretBinding>(activity) {

    override fun getChildLayoutId() = R.layout.dialog_shooter_secret

    override fun initView(binding: DialogShooterSecretBinding) {

        setTitle("完善API密钥")

        setNegativeListener { dismiss() }

        setPositiveListener {
            val secret = binding.shooterSecretEt.text.toString()
            SubtitleConfig.putShooterSecret(secret)
            dismiss()
        }

        binding.shooterSecretEt.setText(SubtitleConfig.getShooterSecret() ?: "")

        binding.loginShooterTv.setOnClickListener {
            activity.startUrlActivity("https://secure.assrt.net/user/logon.xml")
        }
    }
}