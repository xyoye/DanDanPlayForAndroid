package com.xyoye.local_component.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.xyoye.common_component.extension.decodeUrl
import com.xyoye.common_component.utils.StreamHeaderUtil
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogStreamLinkBinding

/**
 * Created by xyoye on 2021/1/22.
 */

class StreamLinkDialog(
    activity: AppCompatActivity,
    private val callback: (link: String, header: Map<String, String>?) -> Unit
) : BaseBottomDialog<DialogStreamLinkBinding>(activity) {

    private lateinit var binding: DialogStreamLinkBinding

    override fun getChildLayoutId() = R.layout.dialog_stream_link

    override fun initView(binding: DialogStreamLinkBinding) {
        this.binding = binding

        setTitle("串流播放")

        setNegativeListener { dismiss() }

        setPositiveListener {
            val result = binding.linkInputEt.text.toString()
            if (result.isEmpty()) {
                ToastCenter.showWarning("链接不能为空")
                return@setPositiveListener
            }

            val headerText = binding.headerInputEt.text.toString()
            val header = StreamHeaderUtil.string2Header(headerText)

            callback.invoke(result.decodeUrl(), header)
            dismiss()
        }

        binding.linkInputEt.hint = "https://"
        binding.linkInputEt.postDelayed({ showKeyboard(binding.linkInputEt) }, 200)

        binding.advancedCb.setOnCheckedChangeListener { _, isChecked ->
            binding.headerInputEt.isVisible = isChecked
            binding.headerInputTips.isVisible = isChecked
            if (isChecked) {
                showKeyboard(binding.headerInputEt)
            }
        }
    }

    override fun dismiss() {
        if (this::binding.isInitialized) {
            hideKeyboard(binding.linkInputEt)
        }
        super.dismiss()
    }
}