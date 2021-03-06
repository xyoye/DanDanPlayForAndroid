package com.xyoye.user_component.ui.fragment

import android.os.Bundle
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.user_component.R

/**
 * Created by xyoye on 2021/2/6.
 */

class SubtitleSettingFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance() = SubtitleSettingFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SubtitleSettingDataStore()
        addPreferencesFromResource(R.xml.preference_subtitle_setting)
    }

    inner class SubtitleSettingDataStore : PreferenceDataStore() {
        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return when (key) {
                "auto_load_local_subtitle" -> SubtitleConfig.isAutoLoadLocalSubtitle()
                "auto_load_network_subtitle" -> SubtitleConfig.isAutoLoadNetworkSubtitle()
                "auto_load_subtitle_network_storage" -> SubtitleConfig.isAutoLoadSubtitleNetworkStorage()
                else -> super.getBoolean(key, defValue)
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "auto_load_local_subtitle" -> SubtitleConfig.putAutoLoadLocalSubtitle(value)
                "auto_load_network_subtitle" -> SubtitleConfig.putAutoLoadNetworkSubtitle(value)
                "auto_load_subtitle_network_storage" -> SubtitleConfig.putAutoLoadSubtitleNetworkStorage(value)
                else -> super.putBoolean(key, value)
            }
        }
    }
}