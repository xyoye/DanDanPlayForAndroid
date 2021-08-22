package com.xyoye.common_component.utils

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

/**
 * Created by xyoye on 2021/7/24.
 */

class DataBindingAdapter {

    companion object {
        @BindingAdapter("app:visible")
        @JvmStatic
        fun visible(view: View, isVisible: Boolean) {
            view.isVisible = isVisible
        }
    }

}