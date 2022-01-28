package com.xyoye.common_component.weight.binding

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

@BindingAdapter("visible")
fun setViewVisible(view: View, visible: Boolean){
    view.isVisible = visible
}