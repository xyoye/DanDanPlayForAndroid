package com.xyoye.common_component.utils

import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.toText
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Created by XYJ on 2021/2/8.
 */

object PlayHistoryUtils {
    private val dayName = arrayOf("今天", "昨天", "前天")

    suspend fun getPlayHistory(uniqueKey: String, mediaType: MediaType): PlayHistoryEntity? {
        return withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.instance.getPlayHistoryDao()
                .getPlayHistory(uniqueKey, mediaType)
        }
    }

    fun formatPlayTime(time: Date): String {
        if (time.after(Date())) {
            return time.toText("yyyy-MM-dd HH:mm")
        }

        val playTime = time.time.toDuration(DurationUnit.MILLISECONDS)
        val currentTime = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)

        val intervalDay = currentTime.minus(playTime).toInt(DurationUnit.DAYS)
        if (intervalDay in dayName.indices) {
            return dayName[intervalDay] + " " + time.toText("HH:mm")
        }
        return time.toText("yyyy-MM-dd HH:mm")
    }

}