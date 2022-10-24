package com.xyoye.common_component.utils.aliyun

import android.app.Application
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.alibaba.sdk.android.man.MANServiceProvider
import com.gyf.immersionbar.ImmersionBar
import com.ta.utdid2.device.UTDevice
import com.taobao.update.adapter.UpdateAdapter
import com.taobao.update.common.framework.UpdateRuntime
import com.taobao.update.datasource.UpdateDataSource
import com.xyoye.common_component.R
import com.xyoye.common_component.extension.isNightMode
import com.xyoye.common_component.extension.toResString
import com.xyoye.common_component.utils.DDLog
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
        initUpdate(application)
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

    private fun initUpdate(application: Application) {
        DDLog.e("TestUpdate", com.ut.device.UTDevice.getUtdid(application))
        //初始化
        val appName = R.string.app_name.toResString()
        UpdateRuntime.init(application, "", appName, "common")
        UpdateDataSource.getInstance().init(
            application,
            "common",
            "",
            false,
            appKey,
            appSecret,
            "",
            UpdateAdapter()
        )
        //不开启缓存
        UpdateDataSource.getInstance().isEnableCache = false
        //执行一次更新检查
        UpdateDataSource.getInstance().startUpdate(false)
    }
}