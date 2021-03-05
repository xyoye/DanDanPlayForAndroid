package com.xyoye.common_component.utils

import com.xyoye.common_component.R
import com.xyoye.data_component.entity.VideoEntity
import java.io.File
import java.util.*

/**
 * Created by xyoye on 2020/11/26.
 */

private val commonVideoExtension = arrayOf(
    "3gp", "avi", "flv", "mp4",
    "m4v", "mkv", "mov", "mpeg",
    "mpg", "mpe", "rm", "rmvb",
    "wmv", "asf", "asx", "dat",
    "vob", "m3u8"
)

val supportSubtitleExtension = arrayOf(
    "ass", "scc", "stl", "srt",
    "ttml"
)

fun isVideoFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return commonVideoExtension.contains(extension.toLowerCase(Locale.ROOT))
}

fun isSubtitleFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return supportSubtitleExtension.contains(extension.toLowerCase(Locale.ROOT))
}

fun isDanmuFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return "xml" == extension.toLowerCase(Locale.ROOT)
}

fun isTorrentFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return extension.toLowerCase(Locale.ROOT) == "torrent"
}

object MediaUtils {

    fun getMediaTypeCover(filePath: String): Int{
        return when{
            isVideoFile(filePath) -> R.drawable.ic_file_video
            isSubtitleFile(filePath) -> R.drawable.ic_file_subtitle
            isDanmuFile(filePath) -> R.drawable.ic_file_xml
            isTorrentFile(filePath) -> R.drawable.ic_file_torrent
            else -> R.drawable.ic_file_unknow
        }
    }

    /**
     * 扫描文件夹内视频文件
     */
    fun scanVideoFile(folderPath: String): MutableList<VideoEntity> {
        val folderFile = File(folderPath)
        if (!folderFile.exists())
            return mutableListOf()

        val childFileArray = folderFile.listFiles() ?: return mutableListOf()

        val videoEntities = mutableListOf<VideoEntity>()
        childFileArray.forEach {
            if (it.isFile && isVideoFile(it.absolutePath)) {
                videoEntities.add(
                    VideoEntity(
                        0,
                        0,
                        0,
                        it.absolutePath,
                        folderPath,
                        null,
                        null,
                        0,
                        it.length(),
                        isFilter = false,
                        isExtend = true
                    )
                )
            } else if (it.isDirectory){
                videoEntities.addAll(scanVideoFile(it.absolutePath))
            }
        }
        return videoEntities
    }
}