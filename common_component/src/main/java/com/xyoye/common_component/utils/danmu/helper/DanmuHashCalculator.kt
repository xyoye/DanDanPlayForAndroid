package com.xyoye.common_component.utils.danmu.helper

import com.xyoye.common_component.extension.toHexString
import java.io.InputStream
import java.security.MessageDigest

/**
 * Created by xyoye on 2023/12/27
 * 读取文件前16M数据，用于计算匹配弹幕所需的Hash值
 */

object DanmuHashCalculator {

    // 匹配弹幕时，计算Hash需要的文件大小
    private const val HASH_CALCULATE_SIZE = 16 * 1024 * 1024

    // 计算Hash每次读取的数据大小
    private const val HASH_BUFFER_SIZE = 256 * 1024

    // MD5计算器
    private val MD5 = MessageDigest.getInstance("MD5")

    /**
     * 读取InputStream的前16M数据，生成hash值
     */
    fun calculate(inputStream: InputStream, bufferSize: Int = HASH_BUFFER_SIZE): String? {
        val buffer = ByteArray(bufferSize)

        var total = 0
        var current: Int
        var target = buffer.size

        return try {
            MD5.reset()
            while (
                inputStream.read(buffer, 0, target).also { current = it } != -1
                && total < HASH_CALCULATE_SIZE
            ) {
                MD5.update(buffer, 0, current)

                total += current
                target = minOf(HASH_CALCULATE_SIZE - total, bufferSize)
            }
            MD5.digest().toHexString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}