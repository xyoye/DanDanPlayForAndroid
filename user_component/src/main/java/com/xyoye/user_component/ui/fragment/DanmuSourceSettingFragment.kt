package com.xyoye.user_component.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.user_component.R

class DanmuSourceSettingFragment : PreferenceFragmentCompat() {

    companion object {
        val languageData = mapOf(
            Pair("默认", "0"),
            Pair("简体", "1"),
            Pair("繁体", "2")
        )

        fun newInstance() = DanmuSourceSettingFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DanmuSourceSettingDataStore()
        addPreferencesFromResource(R.xml.preference_danmu_source_setting)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        findPreference<ListPreference>("danmu_language")?.apply {
            entries = languageData.keys.toTypedArray()
            entryValues = languageData.values.toTypedArray()
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                languageData.forEach {
                    if (it.value == newValue) {
                        summary = it.key
                    }
                }
                return@setOnPreferenceChangeListener true
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    inner class DanmuSourceSettingDataStore : PreferenceDataStore() {
        override fun getString(key: String?, defValue: String?): String? {
            return when (key) {
                "danmu_language" -> DanmuConfig.getDefaultLanguage().toString()
                else -> super.getString(key, defValue)
            }
        }

        override fun putString(key: String?, value: String?) {
            if (value != null) {
                when (key) {
                    "danmu_language" -> DanmuConfig.putDefaultLanguage(value.toInt())
                    else -> super.putString(key, value)
                }
            } else {
                super.putString(key, value)
            }
        }

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return when (key) {
                "show_third_party_danmu" -> DanmuConfig.isShowThirdSource()
                else -> super.getBoolean(key, defValue)
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "show_third_party_danmu" -> DanmuConfig.putShowThirdSource(value)
                else -> super.putBoolean(key, value)
            }
        }
    }
}