package com.xyoye.common_component.utils.subtitle

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.seven_zip.SevenZipUtils
import com.xyoye.data_component.data.SubtitleThunderData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.*


/**
 * Created by xyoye on 2020/12/1.
 */

object SubtitleUtils {

    suspend fun matchSubtitleSilence(filePath: String): String? {
        return withContext(Dispatchers.IO) {
            val videoHash = SubtitleHashUtils.getThunderHash(filePath)
            if (videoHash != null) {
                //从迅雷匹配字幕
                val thunderUrl = "http://sub.xmp.sandai.net:8000/subxl/$videoHash.json"
                var subtitleData: SubtitleThunderData? = null
                try {
                    subtitleData = Retrofit.extService.matchThunderSubtitle(thunderUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //字幕内容存在
                subtitleData?.sublist?.let {
                    val subtitleName = it[0].sname
                    val subtitleUrl = it[0].surl
                    if (subtitleName != null && subtitleUrl != null) {
                        try {
                            //下载保存字幕
                            val responseBody = Retrofit.extService.downloadResource(subtitleUrl)
                            return@withContext saveSubtitle(
                                subtitleName,
                                responseBody.byteStream()
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            return@withContext null
        }
    }

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