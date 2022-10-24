package com.xyoye.common_component.base.app

import android.app.Application
import android.content.Context
import android.os.Handler
import com.alibaba.android.arouter.launcher.ARouter
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.xyoye.common_component.BuildConfig
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.utils.ActivityHelper
import com.xyoye.common_component.utils.SecurityHelper
import com.xyoye.common_component.utils.aliyun.EMASHelper

/**
 * Created by xyoye on 2020/4/13.
 */

open class BaseApplication : Application() {
    companion object {

        private var APPLICATION_CONTEXT: Application? = null
        private var mMainHandler: Handler? = null

        fun getAppContext(): Context {
            return APPLICATION_CONTEXT!!
        }

        fun getMainHandler(): Handler {
            return mMainHandler!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        APPLICATION_CONTEXT = this
        mMainHandler = Handler(getAppContext().mainLooper)

        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        MMKV.initialize(this)
        ARouter.init(this)
        CrashReport.initCrashReport(
            this,
            SecurityHelper.getInstance().buglyId,
            BuildConfig.DEBUG
        )
        Notifications.setupNotificationChannels(this)
        ActivityHelper.instance.init(this)
        EMASHelper.init(this)
    }
}