package com.xyoye.user_component.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val loadSameSubtitleSwitch = findPreference<SwitchPreference>("auto_load_same_name_subtitle")
        val sameSubtitlePriority = findPreference<EditTextPreference>("same_name_subtitle_priority")

        loadSameSubtitleSwitch?.setOnPreferenceChangeListener { _, newValue ->
            sameSubtitlePriority?.isVisible = newValue as Boolean
            return@setOnPreferenceChangeListener true
        }

        sameSubtitlePriority?.apply {
            isVisible = loadSameSubtitleSwitch?.isChecked ?: false
            summary = if (TextUtils.isEmpty(this.text)) "未设置" else text
            setOnPreferenceChangeListener { _, newValue ->
                summary = if (TextUtils.isEmpty(newValue as String?)) "未设置" else newValue
                return@setOnPreferenceChangeListener true
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    inner class SubtitleSettingDataStore : PreferenceDataStore() {
        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return when (key) {
                "auto_load_same_name_subtitle" -> SubtitleConfig.isAutoLoadSameNameSubtitle()
                "auto_match_subtitle" -> SubtitleConfig.isAutoMatchSubtitle()
                else -> super.getBoolean(key, defValue)
            }
        }

        override fun getString(key: String?, defValue: String?): String? {
            return when (key) {
                "same_name_subtitle_priority" -> SubtitleConfig.getSubtitlePriority()
                else -> super.getString(key, defValue)
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "auto_load_same_name_subtitle" -> SubtitleConfig.putAutoLoadSameNameSubtitle(value)
                "auto_match_subtitle" -> SubtitleConfig.putAutoMatchSubtitle(value)
                else -> super.putBoolean(key, value)
            }
        }

        override fun putString(key: String?, value: String?) {
            when (key) {
                "same_name_subtitle_priority" -> SubtitleConfig.putSubtitlePriority(value ?: "")
                else -> super.putString(key, value)
            }
        }
    }
}