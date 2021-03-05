package com.xyoye.common_component.base.app

import android.content.Context
import androidx.startup.Initializer
import com.tencent.mmkv.MMKV

class BaseInitializer : Initializer<Unit>{

    override fun create(context: Context) {
        MMKV.initialize(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }

}