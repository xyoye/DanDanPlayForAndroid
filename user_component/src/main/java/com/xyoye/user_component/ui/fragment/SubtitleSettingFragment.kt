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
                "auto_load_same_name_subtitle" -> SubtitleConfig.isAutoLoadSameNameSubtitle()
                "auto_match_subtitle" -> SubtitleConfig.isAutoMatchSubtitle()
                else -> super.getBoolean(key, defValue)
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "auto_load_same_name_subtitle" -> SubtitleConfig.putAutoLoadSameNameSubtitle(value)
                "auto_match_subtitle" -> SubtitleConfig.putAutoMatchSubtitle(value)
                else -> super.putBoolean(key, value)
            }
        }
    }
}