package com.xyoye.common_component.utils.subtitle

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.seven_zip.SevenZipUtils
import okio.IOException
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


/**
 * Created by xyoye on 2020/12/1.
 */

object SubtitleUtils {

    fun saveSubtitle(
        fileName: String,
        inputStream: InputStream,
        directoryName: String? = null,
    ): String? {
        val directory = if (directoryName != null && directoryName.isNotEmpty()) {
            val directory = File(PathHelper.getSubtitleDirectory(), directoryName)
            if (directory.exists().not()) {
                directory.mkdirs()
            }
            directory
        } else {
            PathHelper.getSubtitleDirectory()
        }

        val subtitleFileName = fileName.formatFileName()
        val subtitleFile = File(directory, subtitleFileName)
        if (subtitleFile.exists()) {
            subtitleFile.delete()
        }

        var outputStream: OutputStream? = null
        try {
            subtitleFile.createNewFile()
            outputStream = BufferedOutputStream(FileOutputStream(subtitleFile, false))
            val data = ByteArray(512 * 1024)
            var len: Int
            while (inputStream.read(data).also { len = it } != -1) {
                outputStream.write(data, 0, len)
            }
            outputStream.flush()
            return subtitleFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }
    }

    suspend fun saveAndUnzipFile(
        fileName: String,
        inputStream: InputStream
    ): String? {
        //创建压缩文件
        var outputStream: OutputStream? = null
        val zipFile = File(PathHelper.getSubtitleDirectory(), fileName.formatFileName())
        if (zipFile.exists()) {
            zipFile.delete()
        }
        try {
            //保存
            zipFile.createNewFile()
            outputStream = BufferedOutputStream(FileOutputStream(zipFile, false))
            val data = ByteArray(512 * 1024)
            var len: Int
            while (inputStream.read(data).also { len = it } != -1) {
                outputStream.write(data, 0, len)
            }
            outputStream.flush()

            //解压
            return SevenZipUtils.extractFile(zipFile)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }
        return null
    }
}