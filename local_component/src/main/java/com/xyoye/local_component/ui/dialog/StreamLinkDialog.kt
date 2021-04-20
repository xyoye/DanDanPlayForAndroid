package com.xyoye.local_component.ui.dialog

import androidx.core.view.isVisible
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.common_component.utils.StreamHeaderUtil
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogStreamLinkBinding

/**
 * Created by xyoye on 2021/1/22.
 */

class StreamLinkDialog : BaseBottomDialog<DialogStreamLinkBinding> {
    private lateinit var callback: (link: String, header: Map<String, String>?) -> Unit

    constructor() : super()

    constructor(
        callback: (link: String, header: Map<String, String>?) -> Unit
    ) : super(true) {
        this.callback = callback
    }

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

            callback.invoke(result, header)
            dismiss()
        }

        binding.linkInputEt.hint = "https://"
        binding.linkInputEt.postDelayed({ showKeyboard(binding.linkInputEt) }, 200)

        //VLC内核不支持手动添加请求头
        if (PlayerConfig.getUsePlayerType() == PlayerType.TYPE_VLC_PLAYER.value){
            val checkBoxText = getString(R.string.text_advanced_link) + "（VLC不支持）"
            binding.advancedCb.text = checkBoxText
            binding.advancedCb.isEnabled = false
        }

        binding.advancedCb.setOnCheckedChangeListener { _, isChecked ->
            binding.headerInputEt.isVisible = isChecked
            binding.headerInputTips.isVisible = isChecked
            if (isChecked) {
                showKeyboard(binding.headerInputEt)
            }
        }
    }

    override fun dismiss() {
        if (this::binding.isInitialized){
            hideKeyboard(binding.linkInputEt)
        }
        super.dismiss()
    }
}