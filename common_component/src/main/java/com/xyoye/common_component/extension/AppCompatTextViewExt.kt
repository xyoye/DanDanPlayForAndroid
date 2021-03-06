package com.xyoye.common_component.extension

import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat

/**
 * Created by xyoye on 2021/3/3.
 */

fun AppCompatTextView.setAutoSizeText(
    text: String?,
    minSizeSp: Int = 12,
    maxSizeSp: Int = 16
){
    //取消自动大小
    TextViewCompat.setAutoSizeTextTypeWithDefaults(
        this,
        TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
    )
    setText(text)
    //开启自动大小
    TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
        this,
        minSizeSp,
        maxSizeSp,
        1,
        TypedValue.COMPLEX_UNIT_SP
    )
}