package com.xyoye.player_component.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.data_component.enums.DanmakuLanguage
import com.xyoye.player.controller.VideoController
import com.xyoye.player.info.PlayerInitializer

/**
 * Player-side danmu config receiver that parses string values, persists them
 * to DanmuConfig and updates PlayerInitializer + VideoController.
 */
class PlayerDanmuConfigReceiver(
    private val videoController: VideoController
) : BroadcastReceiver() {

    companion object {
        const val ACTION = "com.xyoye.dandanplay.ACTION_DANMU_CONFIG_UPDATED"
        const val EXTRA_KEY = "extra_config_key"
        const val EXTRA_VALUE = "extra_config_value"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ACTION) return

        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val valueStr = intent.getStringExtra(EXTRA_VALUE) ?: return

        applyDanmuConfigFromString(key, valueStr)
    }

    private fun applyDanmuConfigFromString(key: String, valueStr: String) {
        when (key) {
            "danmuSize" -> {
                val value = valueStr.toIntOrNull() ?: return
                DanmuConfig.putDanmuSize(value)
                PlayerInitializer.Danmu.size = value
                videoController.getDanmuController().updateDanmuSize()
            }
            "danmuSpeed" -> {
                val value = valueStr.toIntOrNull() ?: return
                DanmuConfig.putDanmuSpeed(value)
                PlayerInitializer.Danmu.speed = value
                videoController.getDanmuController().updateDanmuSpeed()
            }
            "danmuAlpha" -> {
                val value = valueStr.toIntOrNull() ?: return
                DanmuConfig.putDanmuAlpha(value)
                PlayerInitializer.Danmu.alpha = value
                videoController.getDanmuController().updateDanmuAlpha()
            }
            "danmuStoke" -> {
                val value = valueStr.toIntOrNull() ?: return
                DanmuConfig.putDanmuStoke(value)
                PlayerInitializer.Danmu.stoke = value
                videoController.getDanmuController().updateDanmuStoke()
            }
            "showMobileDanmu" -> {
                val value = valueStr.toBoolean()
                DanmuConfig.putShowMobileDanmu(value)
                PlayerInitializer.Danmu.mobileDanmu = value
                videoController.getDanmuController().updateMobileDanmuState()
            }
            "showTopDanmu" -> {
                val value = valueStr.toBoolean()
                DanmuConfig.putShowTopDanmu(value)
                PlayerInitializer.Danmu.topDanmu = value
                videoController.getDanmuController().updateTopDanmuState()
            }
            "showBottomDanmu" -> {
                val value = valueStr.toBoolean()
                DanmuConfig.putShowBottomDanmu(value)
                PlayerInitializer.Danmu.bottomDanmu = value
                videoController.getDanmuController().updateBottomDanmuState()
            }
            "danmuMaxCount" -> {
                val value = valueStr.toIntOrNull() ?: return
                DanmuConfig.putDanmuMaxCount(value)
                PlayerInitializer.Danmu.maxNum = value
                videoController.getDanmuController().updateMaxScreenNum()
            }
            "danmuMaxLine", "danmuScrollMaxLine", "danmuTopMaxLine", "danmuBottomMaxLine" -> {
                val maxLine = valueStr.toIntOrNull() ?: return
                when (key) {
                    "danmuScrollMaxLine" -> DanmuConfig.putDanmuScrollMaxLine(maxLine)
                    "danmuTopMaxLine" -> DanmuConfig.putDanmuTopMaxLine(maxLine)
                    "danmuBottomMaxLine" -> DanmuConfig.putDanmuBottomMaxLine(maxLine)
                }
                when (key) {
                    "danmuScrollMaxLine" -> PlayerInitializer.Danmu.maxScrollLine = maxLine
                    "danmuTopMaxLine" -> PlayerInitializer.Danmu.maxTopLine = maxLine
                    "danmuBottomMaxLine" -> PlayerInitializer.Danmu.maxBottomLine = maxLine
                }
                videoController.getDanmuController().updateMaxLine()
            }
            "cloudDanmuBlock" -> {
                val value = valueStr.toBoolean()
                DanmuConfig.putCloudDanmuBlock(value)
                PlayerInitializer.Danmu.cloudBlock = value
            }
            "danmuLanguage" -> {
                val value = valueStr.toIntOrNull() ?: return
                DanmuConfig.putDanmuLanguage(value)
                PlayerInitializer.Danmu.language = DanmakuLanguage.formValue(value)
            }
        }
    }
}
