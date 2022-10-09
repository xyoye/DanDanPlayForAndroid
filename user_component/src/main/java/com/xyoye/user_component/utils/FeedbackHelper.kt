package com.xyoye.user_component.utils

import android.app.Application
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.extension.isNightMode
import com.xyoye.common_component.utils.SecurityHelper

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/10/9
 *     desc  :
 * </pre>
 */

object FeedbackHelper {
    private var isInitial = false

    fun init(application: Application) {
        if (isInitial) {
            return
        }
        isInitial = true

        FeedbackAPI.init(application, "333777779", SecurityHelper.getInstance().aliyunSecret)
        FeedbackAPI.setActivityCallback {
            ImmersionBar.with(it)
                .statusBarColor(com.xyoye.common_component.R.color.status_bar_color)
                .fitsSystemWindows(true)
                .statusBarDarkFont(!application.isNightMode())
                .init()
        }
    }
}