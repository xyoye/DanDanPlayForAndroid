package com.xyoye.stream_component.utils

import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.PathHelper
import java.io.*

/**
 * Created by xyoye on 2021/5/3.
 */

object WebDavHashUtils {
    //目标长度为前16M，17是容错
    private const val TARGET_LENGTH = 17 * 1024 * 1024

    fun getWebDavHash(inputStream: InputStream): String? {

        val davFile = File(PathHelper.getPlayCacheDirectory(), "dav_cache.part")
        if (davFile.exists()) {
            davFile.delete()
        }

        var outputStream: OutputStream? = null
        try {
            davFile.createNewFile()
            outputStream = BufferedOutputStream(FileOutputStream(davFile, false))
            val data = ByteArray(512 * 1024)
            var len: Int
            var totalLen = 0
            //仅保存目标长度的数据
            while (
                inputStream.read(data).also {
                    len = it
                    totalLen += it
                } != -1 && totalLen <= TARGET_LENGTH
            ) {
                outputStream.write(data, 0, len)
            }
            outputStream.flush()
            return IOUtils.getStreamHash(FileInputStream(davFile))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }
    }
}