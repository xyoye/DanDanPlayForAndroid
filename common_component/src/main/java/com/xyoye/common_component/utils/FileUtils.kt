package com.xyoye.common_component.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.extension.isValid
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


/**
 * Created by xyoye on 2020/9/7.
 */

/**
 * 获取父文件夹路径
 */
fun getDirPath(filePath: String): String {
    if (filePath.isEmpty())
        return ""
    val lastSep = filePath.lastIndexOf(File.separator)
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
fun getParentFolderName(filePath: String): String {
    return getFolderName(getDirPath(filePath))
}

/**
 * 通过路径获取文件夹名
 */
fun getFolderName(folderPath: String): String {
    var tempFolderPath = folderPath

    if (tempFolderPath.isEmpty()) return ""

    while (tempFolderPath.endsWith("/"))
        tempFolderPath = tempFolderPath.substring(0, tempFolderPath.length - 1)

    val index = tempFolderPath.lastIndexOf("/")
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
 * 字节数字转16进制
 */
fun buffer2Hex(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size): String {
    val stringBuilder = StringBuilder(2 * length)
    val k: Int = offset + length
    for (index in offset until k) {
        val hexInt = bytes[index].toInt() and 0xff
        var hexStr = Integer.toHexString(hexInt)
        if (hexStr.length < 2) {
            hexStr = "0$hexStr"
        }
        stringBuilder.append(hexStr)
    }
    return stringBuilder.toString()
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

