package com.xyoye.dandanplay.app

import android.content.Context
import androidx.multidex.MultiDex
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.BuildConfig
import com.xyoye.common_component.base.app.BaseApplication

/**
 * Created by xyoye on 2020/7/27.
 */

class IApplication : BaseApplication(){

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}