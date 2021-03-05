package com.xyoye.stream_component.utils

import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by XYJ on 2021/2/8.
 */

object PlayHistoryUtils {

    suspend fun getPlayHistory(url: String, mediaType: MediaType): PlayHistoryEntity? {
        return withContext(Dispatchers.IO) {
            val historyList = DatabaseManager.instance.getPlayHistoryDao()
                    .getPlayHistory(url, mediaType)

            if (historyList.size == 1) {
                return@withContext historyList[0]
            } else {
                null
            }
        }
    }

}