package com.xyoye.common_component.utils

import java.io.IOException
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


/**
 * Created by xyoye on 2020/11/30.
 */

object SubtitleHashUtils {

    fun getThunderHash(videoPath: String): String? {
        var file: RandomAccessFile? = null
        try {
            val messageDigest = MessageDigest.getInstance("SHA1")
            file = RandomAccessFile(videoPath, "r")
            val fileLength: Long = file.length()
            if (fileLength < 0xF000) {
                val buffer = ByteArray(0xF000)
                file.seek(0)
                file.read(buffer)
                file.close()
                return buffer2Hex(messageDigest.digest(buffer)).toUpperCase(Locale.ROOT)
            }
            val bufferSize = 0x5000
            val positions = longArrayOf(0, fileLength / 3, fileLength - bufferSize)
            for (i in positions.indices) {
                val position = positions[i]
                val buffer = ByteArray(bufferSize)
                file.seek(position)
                file.read(buffer)
                messageDigest.update(buffer)
            }
            return buffer2Hex(messageDigest.digest()).toUpperCase(Locale.ROOT)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } finally {
            try {
                file?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun getShooterHash(videoPath: String): String? {
        try {
            val stringBuilder = StringBuilder()
            val file = RandomAccessFile(videoPath, "r")
            val fileLength: Long = file.length()
            val positions =
                longArrayOf(4096, fileLength / 3 * 2, fileLength / 3, fileLength - 8192)
            for (position in positions) {
                var buffer = ByteArray(4096)
                if (fileLength < position) {
                    file.close()
                    return stringBuilder.toString()
                }
                file.seek(position)
                val realBufferSize: Int = file.read(buffer)
                buffer = buffer.copyOfRange(0, realBufferSize)
                val messageDigest = MessageDigest.getInstance("MD5")
                val byteArray = messageDigest.digest(buffer)
                stringBuilder.append(buffer2Hex(byteArray))
                stringBuilder.append(";")
            }
            file.close()
            stringBuilder.deleteCharAt(stringBuilder.length - 1)
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }
}