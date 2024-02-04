package com.xyoye.user_component.ui.dialog

import android.app.Activity
import com.xyoye.common_component.utils.meida.VideoExtension
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.DialogVideoExtensionSupportSettingBinding

/**
 * Created by xyoye on 2024/2/4
 */

class VideoExtensionSupportSettingDialog(
    activity: Activity
) : BaseBottomDialog<DialogVideoExtensionSupportSettingBinding>(activity) {

    override fun getChildLayoutId() = R.layout.dialog_video_extension_support_setting

    override fun initView(binding: DialogVideoExtensionSupportSettingBinding) {

        setTitle("设置支持的视频扩展名")

        binding.etVideoExtension.setText(VideoExtension.supportText)

        binding.tvResetExtension.setOnClickListener { resetExtension(binding) }

        setNegativeListener { dismiss() }

        setPositiveListener { updateExtension(binding) }
    }

    private fun resetExtension(binding: DialogVideoExtensionSupportSettingBinding) {
        VideoExtension.resetDefault()
        binding.etVideoExtension.setText(VideoExtension.supportText)
    }

    private fun updateExtension(binding: DialogVideoExtensionSupportSettingBinding) {
        val extensionText = binding.etVideoExtension.text.toString()
        if (extensionText.isEmpty()) {
            ToastCenter.showError("输入的扩展名不能为空")
            return
        }
        val updateSuccess = VideoExtension.update(extensionText)
        if (updateSuccess.not()) {
            ToastCenter.showError("输入的内容有误")
            return
        }
        dismiss()
    }
}