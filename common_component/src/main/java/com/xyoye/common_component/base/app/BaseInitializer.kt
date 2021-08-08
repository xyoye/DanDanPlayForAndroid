package com.xyoye.common_component.base.app

import android.content.Context
import androidx.startup.Initializer

class BaseInitializer : Initializer<Unit>{

    override fun create(context: Context) {

    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }

}