package com.xyoye.anime_component.ui.dialog.date_picker

import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.DialogDatePickerBinding
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import java.util.*

/**
 * Created by xyoye on 2020/10/14.
 */

class DatePickerDialog : BaseBottomDialog<DialogDatePickerBinding> {

    private var defaultYear: Int = -1
    private lateinit var block: (Int) -> Unit

    constructor() : super()

    constructor(
        defaultYear: Int = -1,
        block: (Int) -> Unit
    ) : super(true) {
        this.defaultYear = defaultYear
        this.block = block
    }

    override fun getChildLayoutId() = R.layout.dialog_date_picker

    override fun initView(binding: DialogDatePickerBinding) {
        //初始化数据
        val nowYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.pickerView.run {
            label = "年"
            maxValue = nowYear
            minValue = 1980
            value = if (defaultYear > 0) defaultYear else nowYear - 3
            wrapSelectorWheel = false
        }

        setTitle("选择年份")

        setNegativeListener { dismiss() }

        setPositiveListener {
            block.invoke(binding.pickerView.value)
            dismiss()
        }
    }
}