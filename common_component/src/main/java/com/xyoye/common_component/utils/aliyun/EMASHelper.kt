package com.xyoye.common_component.utils.aliyun

import android.app.Application
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.alibaba.sdk.android.man.MANServiceProvider
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.R
import com.xyoye.common_component.extension.isNightMode
import com.xyoye.common_component.utils.SecurityHelper

/**
 * Created by xyoye on 2022/10/22.
 */

object EMASHelper {
    private const val appKey = "333777779"
    private val appSecret = SecurityHelper.getInstance().aliyunSecret

    fun init(application: Application) {
        initFeedback(application)
        initAnalyze(application)
        initAnalyze(application)
    }

    private fun initFeedback(application: Application) {
        FeedbackAPI.init(application, appKey, appSecret)
        FeedbackAPI.setActivityCallback {
            ImmersionBar.with(it)
                .statusBarColor(R.color.status_bar_color)
                .fitsSystemWindows(true)
                .statusBarDarkFont(!application.isNightMode())
                .init()
        }
    }

    private fun initAnalyze(application: Application) {
        MANServiceProvider.getService()?.apply {
            manAnalytics.init(application, application, appKey, appSecret)
        }
    }
}