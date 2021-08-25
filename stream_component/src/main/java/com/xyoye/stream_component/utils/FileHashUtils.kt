package com.xyoye.stream_component.utils

import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.buffer2Hex
import java.io.*
import java.security.MessageDigest

/**
 * Created by xyoye on 2021/5/3.
 */

object FileHashUtils {
    private val messageDigest = MessageDigest.getInstance("MD5")
    //目标长度为前16M，17是容错
    private const val TARGET_LENGTH = 17 * 1024 * 1024

    /**
     * 将流数据保存到本地后再计算Hash
     */
    fun getHash(inputStream: InputStream): String? {
        val cacheFile = File(PathHelper.getPlayCacheDirectory(), "hash.cache")
        if (cacheFile.exists()) {
            cacheFile.delete()
        }

        var outputStream: OutputStream? = null
        try {
            cacheFile.createNewFile()
            outputStream = BufferedOutputStream(FileOutputStream(cacheFile, false))
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
            return getStreamHash(FileInputStream(cacheFile))
        } catch (t: Throwable) {
            t.printStackTrace()
            return null
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }
    }

    /**
     * 通过数据流获取文件hash值
     */
    private fun getStreamHash(inputStream: InputStream?, close: Boolean = true): String? {
        if (inputStream == null)
            return null

        var hash: String? = null
        try {
            val data = ByteArray(1024 * 1024)
            var readLength = inputStream.read(data)
            var totalLength = readLength

            while (readLength > 0 && totalLength <= 16 * 1024 * 1024){
                messageDigest.update(data)
                readLength = inputStream.read(data)
                totalLength += readLength
            }
            hash = buffer2Hex(messageDigest.digest())
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            if (close){
                IOUtils.closeIO(inputStream)
            }
        }
        return hash
    }
}