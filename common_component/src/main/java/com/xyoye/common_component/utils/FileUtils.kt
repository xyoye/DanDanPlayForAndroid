package com.xyoye.common_component.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.extension.isValid
import com.xyoye.common_component.storage.file.StorageFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


/**
 * Created by xyoye on 2020/9/7.
 */

/**
 * 获取父文件夹路径
 */
fun getDirPath(filePath: String, separator: String = File.separator): String {
    if (filePath.isEmpty())
        return ""
    val lastSep = filePath.lastIndexOf(separator)
    return if (lastSep == -1) "" else filePath.substring(0, lastSep)
}

/**
 * 通过文件获取文件名，包括文件扩展名
 */
fun getFileName(file: File?): String {
    return if (file == null) "" else getFileName(file.absolutePath)
}

/**
 * 通过路径获取文件名，包括文件扩展名
 */
fun getFileName(filePath: String?): String {
    if (filePath.isNullOrEmpty()) return ""
    val lastSep: Int = filePath.lastIndexOf(File.separator)
    return if (lastSep == -1) filePath else filePath.substring(lastSep + 1)
}

/**
 * 通过文件获取文件名，不包括文件扩展名
 */
fun getFileNameNoExtension(file: File?): String {
    return if (file == null) "" else getFileNameNoExtension(file.absolutePath)
}

/**
 * 通过路径获取文件名，不包括文件扩展名
 */
fun getFileNameNoExtension(filePath: String?): String {
    if (filePath.isNullOrEmpty()) return ""
    val lastPoi = filePath.lastIndexOf('.')
    val lastSep = filePath.lastIndexOf(File.separator)
    if (lastSep == -1) {
        return if (lastPoi == -1) filePath else filePath.substring(0, lastPoi)
    }
    return if (lastPoi == -1 || lastSep > lastPoi) {
        filePath.substring(lastSep + 1)
    } else
        filePath.substring(lastSep + 1, lastPoi)
}

/**
 * 通过路径获取父文件夹名
 */
fun getParentFolderName(filePath: String, separator: String = File.separator): String {
    return getFolderName(getDirPath(filePath, separator), separator)
}
/**
 * 通过路径获取文件夹名
 */
fun getFolderName(folderPath: String, separator: String = File.separator): String {
    var tempFolderPath = folderPath

    if (tempFolderPath.isEmpty()) return ""

    while (tempFolderPath.endsWith(separator))
        tempFolderPath = tempFolderPath.substring(0, tempFolderPath.length - 1)

    val index = tempFolderPath.lastIndexOf(separator)
    return if (index > 0 && index + 1 < tempFolderPath.length) {
        tempFolderPath.substring(index + 1)
    } else {
        tempFolderPath
    }
}

/**
 * 检查文件是否存在
 */
fun isFileExist(filePath: String?): Boolean {
    if (filePath.isNullOrEmpty())
        return false
    if (File(filePath).isValid())
        return true

    if (Build.VERSION.SDK_INT >= 29) {
        try {
            val uri = Uri.parse(filePath)
            val cr: ContentResolver = BaseApplication.getAppContext().contentResolver
            val afd = cr.openAssetFileDescriptor(uri, "r") ?: return false
            try {
                afd.close()
            } catch (ignore: IOException) {
            }
        } catch (e: FileNotFoundException) {
            return false
        }
        return true
    }
    return false
}

/**
 * 获取文件格式
 */
fun getFileExtension(file: File): String {
    return getFileExtension(file.path)
}

/**
 * 获取文件格式
 */
fun getFileExtension(filePath: String?): String {
    if (filePath.isNullOrEmpty()) return ""
    val lastPoi = filePath.lastIndexOf('.')
    val lastSep = filePath.lastIndexOf(File.separator)
    return if (lastPoi == -1 || lastSep >= lastPoi) "" else filePath.substring(lastPoi + 1)
}

/**
 * 文件大小格式化
 */
fun formatFileSize(size: Long): String {
    val kb: Long = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        size >= gb -> {
            String.format("%.1f GB", size.toFloat() / gb)
        }
        size >= mb -> {
            val f = size.toFloat() / mb
            String.format(if (f > 100) "%.0f M" else "%.1f M", f)
        }
        size >= kb -> {
            val f = size.toFloat() / kb
            String.format(if (f > 100) "%.0f K" else "%.1f K", f)
        }
        else -> String.format("%d B", size)
    }
}

private val SEASON_REGEX = Regex("^Season \\d+\$")

/**
 * 获取易于辨识的目录名。
 * 对于符合 kodi 电视剧媒体库结构的视频目录（格式为 Season XX），会返回 <pre>目录名 (Season XX)</pre>。
 * 否则，仍然直接返回目录名。
 */
fun getRecognizableFileName(storageFile: StorageFile): String {
    return if (storageFile.isDirectory() && storageFile.fileName().matches(SEASON_REGEX)) {
        // For kodi TV show library's season directory, add the parent directory name as the prefix
        val parent = try { Uri.parse(storageFile.filePath()).pathSegments } catch (e: Exception) { null }
            ?.takeIf { it.size > 1 }
            ?.let { it[it.size - 2] }
        if (parent != null) "$parent (${storageFile.fileName()})" else storageFile.fileName()
    } else storageFile.fileName()
}
