package com.xyoye.storage_component.providers

import android.content.Context
import android.os.Build
import androidx.startup.Initializer
import com.xunlei.downloadlib.XLTaskHelper
import com.xyoye.common_component.base.app.BaseInitializer
import com.xyoye.common_component.utils.thunder.ThunderManager

class ThunderInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val supportXL = ThunderManager.SUPPORTED_ABI.any { Build.SUPPORTED_ABIS.contains(it) }
        if (supportXL) {
            XLTaskHelper.init(context)
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf(BaseInitializer::class.java)
    }

}