package com.xyoye.anime_component.ui.dialog.date_picker

import androidx.appcompat.app.AppCompatActivity
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.DialogDatePickerBinding
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import java.util.*

/**
 * Created by xyoye on 2020/10/14.
 */

class DatePickerDialog(
    activity: AppCompatActivity,
    private val defaultYear: Int = -1,
    private val block: (Int) -> Unit
) : BaseBottomDialog<DialogDatePickerBinding>(activity) {

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