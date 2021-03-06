package com.xyoye.common_component.extension

import android.content.Context
import android.content.res.Configuration
import androidx.fragment.app.Fragment

val Context.isDark
    get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

val Fragment.isDark
    get() = requireContext().isDark