package com.xyoye.common_component.resolver

import android.annotation.SuppressLint
import android.database.Cursor
import android.provider.MediaStore
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.getDirPath
import com.xyoye.data_component.entity.VideoEntity

/**
 * Created by xyoye on 2020/7/29.
 */

class MediaResolver {
    companion object {
        @SuppressLint("InlinedApi")
        private val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            //AndroidQ上测试可正常获取数据，如果后面有反馈再优化
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE,
            //AndroidQ以下设备可正常获取数据，如果后面有反馈再优化
            MediaStore.Video.Media.DURATION
        )

        fun queryVideo(): MutableList<VideoEntity> {
            val cursor = BaseApplication.getAppContext().contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                null,
                null
            )
            return readVideoFormCursor(cursor)
        }

        private fun readVideoFormCursor(cursor: Cursor?): MutableList<VideoEntity> {
            val videoList = mutableListOf<VideoEntity>()
            if (cursor == null){
                return videoList
            }
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val filePath = cursor.getString(1)
                val size = cursor.getLong(2)
                val duration = cursor.getLong(3)
                videoList.add(
                    VideoEntity(0, id, 0, filePath,
                        getDirPath(filePath), null, null,
                        duration, size
                    )
                )
            }
            cursor.close()
            return videoList
        }
    }
}