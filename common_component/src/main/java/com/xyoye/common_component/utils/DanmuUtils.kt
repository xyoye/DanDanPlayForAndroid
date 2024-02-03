package com.xyoye.common_component.utils

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStreamWriter

/**
 * Created by xyoye on 2020/11/23.
 */

object DanmuUtils {

    fun appendDanmu(danmuPath: String, appendText: String) {
        val danmuFile = File(danmuPath)
        if (!danmuFile.exists())
            return

        val tempFileName = getFileNameNoExtension(danmuPath)
        val tempFileExtension = getFileExtension(danmuFile)
        val tempFile = File(danmuFile.parentFile!!, tempFileName + "_temp.$tempFileExtension")

        var fileReader: FileReader? = null
        var bufferFileReader: BufferedReader? = null

        var fileOutputStream: FileOutputStream? = null
        var bufferedFileWriter: BufferedWriter? = null

        try {
            fileReader = FileReader(danmuFile)
            bufferFileReader = BufferedReader(fileReader)

            fileOutputStream = FileOutputStream(tempFile, false)
            bufferedFileWriter =
                BufferedWriter(OutputStreamWriter(fileOutputStream, Charsets.UTF_8))

            var danmuText: String? = bufferFileReader.readLine()
            while (danmuText != null) {
                if (danmuText == "</i>") {
                    bufferedFileWriter.write(appendText)
                    bufferedFileWriter.newLine()
                    bufferedFileWriter.write(danmuText)
                    bufferedFileWriter.newLine()
                    break
                }

                bufferedFileWriter.write(danmuText)
                bufferedFileWriter.newLine()

                danmuText = bufferFileReader.readLine()
            }
            bufferedFileWriter.flush()

            IOUtils.closeIO(bufferFileReader)
            IOUtils.closeIO(fileReader)

            tempFile.renameTo(danmuFile)
        } catch (e: Throwable) {
            DDLog.i("写入弹幕失败", e)
        } finally {
            IOUtils.closeIO(fileOutputStream)
            IOUtils.closeIO(bufferedFileWriter)
            IOUtils.closeIO(bufferFileReader)
            IOUtils.closeIO(fileReader)
        }
    }
}