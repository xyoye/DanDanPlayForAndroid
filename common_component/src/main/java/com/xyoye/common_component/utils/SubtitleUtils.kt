package com.xyoye.common_component.utils

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.utils.seven_zip.SevenZipUtils
import java.io.*


/**
 * Created by xyoye on 2020/12/1.
 */

object SubtitleUtils {

    fun saveSubtitle(
        fileName: String,
        inputStream: InputStream
    ): String? {
        val subtitleFileName = fileName.formatFileName()
        val subtitleFile = File(PathHelper.getSubtitleDirectory(), subtitleFileName)
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

    fun saveAndUnzipFile(
        fileName: String,
        inputStream: InputStream,
        callback: (destDirPath: String) -> Unit
    ) {
        //创建压缩文件
        val zipFile = File(PathHelper.getSubtitleDirectory(), fileName.formatFileName())
        if (zipFile.exists()) {
            zipFile.delete()
        }
        zipFile.createNewFile()
        //保存
        var outputStream: OutputStream? = null
        try {
            outputStream = BufferedOutputStream(FileOutputStream(zipFile, false))
            val data = ByteArray(512 * 1024)
            var len: Int
            while (inputStream.read(data).also { len = it } != -1) {
                outputStream.write(data, 0, len)
            }
            outputStream.flush()

            //解压
            SevenZipUtils.extractFile(zipFile, callback)
        } catch (e: IOException) {
            e.printStackTrace()
            callback.invoke("")
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }
    }

    fun findLocalSubtitleByVideo(videoPath: String): String? {
        val videoFile = File(videoPath)
        if (!videoFile.exists())
            return null

        //可能存字幕的文件夹
        val subtitleDirectory = mutableListOf(
            //字幕下载目录
            PathHelper.getSubtitleDirectory()
        )
        //视频所在文件夹
        videoFile.parentFile?.let {
            subtitleDirectory.add(it)
        }

        //获取文件名，无后缀
        val videoNameNotExtension = getFileNameNoExtension(videoFile)
        val targetVideoName = "$videoNameNotExtension."

        val possibleList = mutableListOf<String>()

        subtitleDirectory.forEach { directory ->
            if (directory.exists() && directory.isDirectory) {
                //遍历文件夹
                directory.listFiles()?.forEach {
                    //同名字幕的文件
                    if (isSameNameSubtitle(it.absolutePath, targetVideoName)){
                        possibleList.add(it.absolutePath)
                    }
                }
            }
        }

        if (possibleList.size == 1) {
            //只存在一个弹幕
            return possibleList[0]
        } else if (possibleList.size > 1) {
            //存在多个匹配的字幕，可能是：xx.sc.ass、xx.tc.ass
            possibleList.forEach {
                //存在xx.ass，直接返回
                supportSubtitleExtension.forEach { extension ->
                    if ("$videoNameNotExtension.${extension}" == getFileName(it))
                        return it
                }
            }
            //取第一个
            return possibleList[0]
        }
        return null
    }

    fun isSameNameSubtitle(subtitlePath: String, targetVideoName: String): Boolean {
        val subtitleName = getFileNameNoExtension(subtitlePath) + "."
        val videoName = getFileNameNoExtension(targetVideoName) + "."

        //字幕文件名与视频名相同
        if (subtitleName.startsWith(videoName)) {
            val extension: String = getFileExtension(subtitlePath)
            //支持的字幕格式
            return supportSubtitleExtension.find {
                it.equals(extension, true)
            } != null
        }
        return false
    }
}