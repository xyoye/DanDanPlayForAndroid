package com.xyoye.download_component.initializer

import android.content.Context
import androidx.startup.Initializer
import com.xunlei.downloadlib.XLTaskHelper
import com.xyoye.common_component.base.app.BaseInitializer

class ThunderInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        XLTaskHelper.init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf(BaseInitializer::class.java)
    }

}