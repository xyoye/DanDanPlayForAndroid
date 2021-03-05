package com.xyoye.common_component.utils

import okhttp3.Headers
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Created by gaozhen on 2017/12/4.
 */
object GZIPUtils {
    private const val ENCODE_UTF_8 = "UTF-8"
    const val ENCODE_ISO_8859_1 = "ISO-8859-1"

    /**
     * String 压缩至gzip 字节数组，可选择encoding配置
     */
    @JvmOverloads
    fun compress(
        str: String?,
        encoding: String = ENCODE_UTF_8
    ): ByteArray? {
        if (str.isNullOrEmpty()) {
            return null
        }
        val out = ByteArrayOutputStream()
        var gzipInputStream: GZIPOutputStream? = null
        try {
            gzipInputStream = GZIPOutputStream(out)
            gzipInputStream.write(str.toByteArray(charset(encoding)))
            gzipInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            gzipInputStream?.close()
            out.close()
        }
        return out.toByteArray()
    }

    /**
     * 字节数组解压
     */
    fun uncompress(bytes: ByteArray?): ByteArray? {
        if (bytes == null || bytes.isEmpty()) {
            return null
        }
        val out = ByteArrayOutputStream()
        val bis = ByteArrayInputStream(bytes)
        try {
            val gzipInputStream = GZIPInputStream(bis)
            val buffer = ByteArray(256)
            var n: Int
            while (gzipInputStream.read(buffer).also { n = it } >= 0) {
                out.write(buffer, 0, n)
            }
        } catch (e: IOException) {
            println("gzip uncompress error.")
        } finally {
            bis.close()
            out.close()
        }
        return out.toByteArray()
    }

    /**
     * 字节数组解压至string，可选择encoding配置
     */
    @JvmOverloads
    fun uncompressToString(
        bytes: ByteArray?,
        encoding: String = ENCODE_UTF_8
    ): String? {
        if (bytes == null || bytes.isEmpty()) {
            return null
        }
        val out = ByteArrayOutputStream()
        val bis = ByteArrayInputStream(bytes)
        try {
            val unGzip = GZIPInputStream(bis)
            val buffer = ByteArray(256)
            var n: Int
            while (unGzip.read(buffer).also { n = it } >= 0) {
                out.write(buffer, 0, n)
            }
        } catch (e: IOException) {
            println("gzip uncompress to string error")
        } finally {
            bis.close()
            out.close()
        }
        return out.toString(encoding)
    }

    /**
     * 判断请求头是否存在gzip
     */
    fun isGzip(headers: Headers): Boolean {
        for (key in headers.names()) {
            if ((key.equals("Accept-Encoding", ignoreCase = true)
                        && headers[key]?.contains("gzip") == true)
                || (key.equals("Content-Encoding", ignoreCase = true)
                        && headers[key]?.contains("gzip") == true)
            ) {
                return true
            }
        }
        return false
    }
}