package com.xyoye.common_component.utils

import android.content.ContentUris
import android.provider.MediaStore
import java.io.Closeable
import java.io.File
import java.io.IOException

/**
 * Created by xyoye on 2020/12/29.
 */

object IOUtils {

    /**
     * 通过ID获取视频Uri
     */
    fun getVideoUri(id: Long) =
        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

    /**
     * 获取文件夹大小
     */
    fun getDirectorySize(directory: File): Long {
        if (!directory.exists())
            return 0L
        if (directory.isFile)
            return directory.length()

        var totalSize = 0L
        directory.listFiles()?.forEach {
            totalSize += if (it.isDirectory) {
                getDirectorySize(it)
            } else {
                it.length()
            }
        }

        return totalSize
    }

    /**
     * 关闭IO流
     */
    fun closeIO(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (e: IOException) {
            // ignore
        }
    }
}

