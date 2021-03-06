package com.xyoye.common_component.weight.dialog

import android.text.InputType
import com.xyoye.common_component.R
import com.xyoye.common_component.databinding.DialogCommonEditBinding
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.EditBean

/**
 * Created by xyoye on 2021/1/11.
 */

class CommonEditDialog : BaseBottomDialog<DialogCommonEditBinding> {
    private lateinit var editBean: EditBean
    private var inputOnlyDigit: Boolean = false
    private var checkBlock: ((result: String) -> Boolean)? = null
    private lateinit var callback: (result: String) -> Unit

    constructor() : super()

    constructor(
        editBean: EditBean,
        inputOnlyDigit: Boolean = false,
        checkBlock: ((result: String) -> Boolean)? = null,
        callback: (result: String) -> Unit
    ) : super(true) {
        this.editBean = editBean
        this.inputOnlyDigit = inputOnlyDigit
        this.checkBlock = checkBlock
        this.callback = callback
    }

    var onNegativeCallback: (() -> Unit)? = null

    private lateinit var binding: DialogCommonEditBinding

    override fun getChildLayoutId() = R.layout.dialog_common_edit

    override fun initView(binding: DialogCommonEditBinding) {
        this.binding = binding

        setTitle(editBean.title)

        setNegativeListener {
            onNegativeCallback?.invoke()
            dismiss()
        }

        setPositiveListener {
            val result = binding.inputEt.text.toString().trim()
            if (result.isEmpty()) {
                ToastCenter.showWarning(editBean.emptyWarningMsg)
                return@setPositiveListener
            }

            if (checkBlock?.invoke(result) == false) {
                return@setPositiveListener
            }

            callback.invoke(result)
            dismiss()
        }

        if (inputOnlyDigit) {
            binding.inputEt.inputType = InputType.TYPE_CLASS_NUMBER
        }

        binding.inputEt.setText(editBean.defaultText)
        binding.inputEt.hint = editBean.hint
        binding.inputEt.postDelayed({ showKeyboard(binding.inputEt) }, 200)
    }

    override fun dismiss() {
        hideKeyboard(binding.inputEt)
        super.dismiss()
    }
}