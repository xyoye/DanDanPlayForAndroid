package com.xyoye.common_component.utils.meida

import android.media.MediaMetadataRetriever
import android.net.Uri
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.extension.isInvalid
import com.xyoye.common_component.utils.isVideoFile
import com.xyoye.data_component.entity.VideoEntity
import java.io.File

/**
 * Created by xyoye on 2024/2/4
 */

object VideoScan {

    /**
     * 遍历路径内的视频文件
     */
    fun traverse(filePath: String): List<VideoEntity> {
        return try {
            traverse(File(filePath))
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 遍历文件内的视频文件，包括文件自身
     */
    private fun traverse(file: File): List<VideoEntity> {
        if (file.isFile) {
            return if (file.isInvalid()) {
                return emptyList()
            } else if (isVideoFile(file.absolutePath)) {
                listOf(generateVideoEntity(file))
            } else {
                emptyList()
            }
        }

        if (file.exists().not() || file.canRead().not()) {
            return emptyList()
        }
        return file.listFiles()
            ?.flatMap { traverse(it) }
            ?: emptyList()
    }

    /**
     * 生成视频数据库实体
     */
    private fun generateVideoEntity(file: File): VideoEntity {
        return VideoEntity(
            0,
            0,
            0,
            file.absolutePath,
            file.parentFile?.absolutePath.orEmpty(),
            null,
            null,
            getVideoDuration(file),
            file.length(),
            isFilter = false,
            isExtend = true
        )
    }

    /**
     * 获取视频时长
     */
    private fun getVideoDuration(videoFile: File): Long {
        if (videoFile.isInvalid()) {
            return 0
        }
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(BaseApplication.getAppContext(), Uri.fromFile(videoFile))
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        } finally {
            retriever.release()
        }
    }
}