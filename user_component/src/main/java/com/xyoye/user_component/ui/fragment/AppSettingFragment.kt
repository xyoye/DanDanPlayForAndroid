package com.xyoye.user_component.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.AppUtils
import com.xyoye.user_component.R

/**
 * Created by xyoye on 2021/2/23.
 */

class AppSettingFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance() = AppSettingFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = AppSettingDataStore()
        addPreferencesFromResource(R.xml.preference_app_setting)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        findPreference<Preference>("dark_mode")?.apply {
            setOnPreferenceClickListener {
                ARouter.getInstance()
                    .build(RouteTable.User.SwitchTheme)
                    .navigation()
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("app_version")?.apply {
            summary = AppUtils.getVersionName()
            setOnPreferenceClickListener {
                AppUtils.checkUpdate()
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("license")?.apply {
            setOnPreferenceClickListener {
                ARouter.getInstance()
                    .build(RouteTable.User.License)
                    .navigation()
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("about_us")?.apply {
            setOnPreferenceClickListener {
                ARouter.getInstance()
                    .build(RouteTable.User.AboutUs)
                    .navigation()
                return@setOnPreferenceClickListener true
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }


    inner class AppSettingDataStore : PreferenceDataStore() {
        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return when (key) {
                "hide_file" -> AppConfig.isShowHiddenFile()
                "splash_page" -> AppConfig.isShowSplashAnimation()
                else -> super.getBoolean(key, defValue)
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "hide_file" -> AppConfig.putShowHiddenFile(value)
                "splash_page" -> AppConfig.putShowSplashAnimation(value)
            }
        }
    }
}