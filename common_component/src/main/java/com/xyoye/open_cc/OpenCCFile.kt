package com.xyoye.open_cc

import android.content.Context
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.PathHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by xyoye on 2023/5/27
 */

object OpenCCFile {
    // 简转繁配置文件
    val s2t: File = File(PathHelper.getOpenCCDirectory(), "s2t.json")

    // 繁转简配置文件
    val t2s: File = File(PathHelper.getOpenCCDirectory(), "t2s.json")

    // assets中open_cc文件夹名称
    private const val OPEN_CC_ASSETS_DIR = "open_cc"

    var init = false

    /**
     * 初始化open_cc相关文件
     */
    fun init(context: Context) {
        try {
            val openCCDir = PathHelper.getOpenCCDirectory()

            context.assets.list(OPEN_CC_ASSETS_DIR)?.forEach {
                val internalFile = File(openCCDir, it)
                if (internalFile.exists()) {
                    return@forEach
                }

                internalFile.createNewFile()
                val assetsFilePath = "$OPEN_CC_ASSETS_DIR/$it"
                val destFilePath = internalFile.absolutePath
                copyFileFromAssets(context, assetsFilePath, destFilePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 从assets目录中复制文件到本地
     */
    private fun copyFileFromAssets(context: Context, assetsFilePath: String, destFilePath: String) {
        var inputStream: InputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            inputStream = context.assets.open(assetsFilePath)
            fileOutputStream = FileOutputStream(destFilePath)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                fileOutputStream.write(buffer, 0, length)
            }
            fileOutputStream.flush()
        } catch (e: IOException) {
            throw e
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(fileOutputStream)
        }
    }
}