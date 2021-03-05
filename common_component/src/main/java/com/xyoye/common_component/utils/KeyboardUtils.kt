package com.xyoye.common_component.utils

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.xyoye.common_component.base.app.BaseApplication

/**
 * Created by xyoye on 2020/8/19.
 */

fun showKeyboard(view: View, flags: Int = 0) {
    view.isFocusable = true
    view.isFocusableInTouchMode = true
    view.requestFocus()

    (BaseApplication.getAppContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .apply {
            showSoftInput(view, flags, object : ResultReceiver(Handler()) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN
                        || resultCode == InputMethodManager.RESULT_HIDDEN
                    ) {
                        toggleSoftInput(0, 0)
                    }
                }
            })
            toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
}

fun hideKeyboard(view: View) {
    (BaseApplication.getAppContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
    .hideSoftInputFromWindow(view.windowToken, 0)
}