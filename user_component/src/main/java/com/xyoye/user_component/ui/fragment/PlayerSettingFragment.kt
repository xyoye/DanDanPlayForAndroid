package com.xyoye.user_component.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.*
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.data_component.enums.PixelFormat
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.user_component.R

/**
 * Created by xyoye on 2021/2/5.
 */

class PlayerSettingFragment : PreferenceFragmentCompat() {
    companion object {
        fun newInstance() = PlayerSettingFragment()

        val playerData = mapOf(
            Pair("IJK Player", PlayerType.TYPE_IJK_PLAYER.value.toString()),
            Pair("EXO Player", PlayerType.TYPE_EXO_PLAYER.value.toString())
        )

        val pixelData = mapOf(
            Pair("默认格式", PixelFormat.PIXEL_AUTO.value),
            Pair("RGB 565", PixelFormat.PIXEL_RGB565.value),
            Pair("RGB 888", PixelFormat.PIXEL_RGB888.value),
            Pair("RGBX 8888", PixelFormat.PIXEL_RGBX8888.value),
            Pair("YV12", PixelFormat.PIXEL_YV12.value),
            Pair("OpenGL ES2", PixelFormat.PIXEL_OPEN_GL_ES2.value)
        )

        val ijkPreference = arrayOf(
            "media_code_c",
            "media_code_c_h265",
            "open_sl_es",
            "pixel_format_type"
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = PlayerSettingDataStore()
        addPreferencesFromResource(R.xml.preference_player_setting)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //播放器类型
        findPreference<ListPreference>("player_type")?.apply {
            entries = playerData.keys.toTypedArray()
            entryValues = playerData.values.toTypedArray()
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                playerData.forEach {
                    if (it.value == newValue) {
                        summary = it.key

                        ijkPreference.forEach { key ->
                            findPreference<Preference>(key)?.isVisible =
                                newValue == PlayerType.TYPE_IJK_PLAYER.value.toString()
                        }
                    }
                }
                return@setOnPreferenceChangeListener true
            }

            //初始化IJK配置的显示
            ijkPreference.forEach { key ->
                findPreference<Preference>(key)?.isVisible =
                    value == PlayerType.TYPE_IJK_PLAYER.value.toString()
            }
        }

        //像素格式
        findPreference<ListPreference>("pixel_format_type")?.apply {
            entries = pixelData.keys.toTypedArray()
            entryValues = pixelData.values.toTypedArray()
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                pixelData.forEach {
                    if (it.value == newValue) {
                        summary = it.key
                    }
                }
                return@setOnPreferenceChangeListener true
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    inner class PlayerSettingDataStore : PreferenceDataStore() {

        override fun getString(key: String?, defValue: String?): String? {
            return when (key) {
                "pixel_format_type" -> PlayerConfig.getUsePixelFormat()
                "player_type" -> PlayerConfig.getUsePlayerType().toString()
                else -> super.getString(key, defValue)
            }
        }

        override fun putString(key: String?, value: String?) {
            if (value != null) {
                when (key) {
                    "pixel_format_type" -> PlayerConfig.putUsePixelFormat(value)
                    "player_type" -> PlayerConfig.putUsePlayerType(value.toInt())
                    else -> super.putString(key, value)
                }
            } else {
                super.putString(key, value)
            }
        }

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return when (key) {
                "media_code_c" -> PlayerConfig.isUseMediaCodeC()
                "media_code_c_h265" -> PlayerConfig.isUseMediaCodeCH265()
                "open_sl_es" -> PlayerConfig.isUseOpenSlEs()
                "surface_renders" -> PlayerConfig.isUseSurfaceView()
                else -> super.getBoolean(key, defValue)
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "media_code_c" -> PlayerConfig.putUseMediaCodeC(value)
                "media_code_c_h265" -> PlayerConfig.putUseMediaCodeCH265(value)
                "open_sl_es" -> PlayerConfig.putUseOpenSlEs(value)
                "surface_renders" -> PlayerConfig.putUseSurfaceView(value)
                else -> super.putBoolean(key, value)
            }
        }
    }
}