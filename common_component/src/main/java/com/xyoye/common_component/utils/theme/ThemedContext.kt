package com.xyoye.common_component.utils.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.xyoye.common_component.base.app.BaseApplication

/**
 * Created by xyoye on 2022/10/10
 */

@SuppressLint("StaticFieldLeak")
object ThemedContext {

    private var themedContext: Context? = null

    fun get(): Context {
        if (themedContext == null) {
            themedContext = createContext(BaseApplication.getAppContext())
        }
        return themedContext!!
    }

    private fun createContext(application: Context): Context {
        val resources = application.resources
        val configuration = Configuration(resources.configuration)
        val filter = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
        configuration.uiMode = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO or filter
            AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES or filter
            else -> resources.configuration.uiMode
        }
        return application.createConfigurationContext(configuration)
    }
}