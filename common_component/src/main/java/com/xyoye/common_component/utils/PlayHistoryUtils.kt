package com.xyoye.common_component.utils

import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by XYJ on 2021/2/8.
 */

object PlayHistoryUtils {

    suspend fun getPlayHistory(url: String, mediaType: MediaType): PlayHistoryEntity? {
        return withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.instance.getPlayHistoryDao()
                    .getPlayHistory(url, mediaType)
        }
    }

    fun formatPlayTime(time: Date): String {
        val currentTime = System.currentTimeMillis()
        val oneDayTime = 24 * 60 * 60 * 1000

        val interval = currentTime - time.time

        val header = when {
            interval < oneDayTime -> {
                "今天"
            }
            interval < oneDayTime * 2 -> {
                "昨天"
            }
            interval < oneDayTime * 3 -> {
                "前天"
            }
            else -> {
                "${(interval / oneDayTime) + 1}天前"
            }
        }
        val footer = date2Str(time, "HH:mm")
        return "$header $footer"
    }

}