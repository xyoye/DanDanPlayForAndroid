package com.xyoye.common_component.utils

import java.util.*
import kotlin.math.min

/**
 * Created by xyoye on 2020/6/22.
 *
 * 提取两个字符串不同的部分
 * 如果不同的部分是数字，则按数字大小排序
 * 否则按文字排序
 */
class FileComparator<T>(
    private val value: (T) -> String,
    private val isDirectory: (T) -> Boolean,
    private val desc: Boolean = false
) : Comparator<T> {

    override fun compare(o1: T?, o2: T?): Int {
        if (o1 == null)
            return -1
        if (o2 == null)
            return 1

        val isDirectory1 = isDirectory.invoke(o1)
        val isDirectory2 = isDirectory.invoke(o2)

        return when {
            //都是文件夹，或都是文件
            isDirectory1 == isDirectory2 -> {
                compareFileName(o1, o2)
            }
            //文件夹在前
            isDirectory1 && isDirectory2.not() -> -1
            else -> 1
        }
    }

    /**
     * 比较文件名
     */
    private fun compareFileName(o1: T, o2: T): Int {
        //获取文件名
        val str1 = if (desc) value.invoke(o2) else value.invoke(o1)
        val str2 = if (desc) value.invoke(o1) else value.invoke(o2)
        //完全相同的文字
        if (str1 == str2)
            return 0

        //尝试将整体视为Int比较
        val intCompare = compareInt(str1, str2)
        if (intCompare != null) {
            return intCompare
        }

        //是否存在需要对比的扩展名
        val needCompareExt1 = isNeedCompareExtension(str1)
        val needCompareExt2 = isNeedCompareExtension(str2)

        return when {
            needCompareExt1 && needCompareExt2 -> compareFileNameAndExt(str1, str2)
            needCompareExt1.not() && needCompareExt2.not() -> compareText(str1, str2)
            needCompareExt1 -> 1
            else -> -1
        }
    }

    /**
     * 比较文件名，文件名相同时返回文件后缀比较结果
     */
    private fun compareFileNameAndExt(str1: String, str2: String): Int {
        //进入前已确保index有效
        val separatorIndex1 = str1.indexOf(".")
        val separatorIndex2 = str2.indexOf(".")

        val fileName1 = str1.substring(0, separatorIndex1)
        val fileName2 = str2.substring(0, separatorIndex2)

        //优先比较文件名
        val result = compareText(fileName1, fileName2)
        if (result != 0) {
            return result
        }

        //文件名相同，比较扩展名
        val extension1 = str1.substring(separatorIndex1)
        val extension2 = str2.substring(separatorIndex2)
        return compareText(extension1, extension2)
    }

    /**
     * 如果两个字符串都是数字，则作为数字比较
     * @return 比较结果，null则代表至少有一个字符串不能作为数字比较
     */
    private fun compareInt(str1: String, str2: String): Int? {
        val bigInt1 = str1.toBigIntegerOrNull() ?: return null
        val bigInt2 = str2.toBigIntegerOrNull() ?: return null

        val result = bigInt1.compareTo(bigInt2)
        if (result != 0) {
            return result
        }
        //数值相同时，比较字符串长度，较长的在后
        return str1.length.compareTo(str2.length)
    }

    /**
     * 文字比较，删除两个文字前后相同部分后，再进行比较
     */
    private fun compareText(str1: String, str2: String): Int {
        if (str1 == str2)
            return 0

        val diff = getDiffText(str1, str2)
        val diff1 = diff.first
        val diff2 = diff.second

        if (diff1 == diff2) return 0
        if (diff1.isEmpty()) return -1
        if (diff2.isEmpty()) return 1

        //尝试将整体视为Int比较
        val intCompare = compareInt(diff1, diff2)
        if (intCompare != null) {
            return intCompare
        }
        return diff1.compareTo(diff2)
    }

    /**
     * 获取两个文字不相同部分
     * 通过移除前后相同部分得到
     */
    private fun getDiffText(str1: String, str2: String): Pair<String, String> {
        if (str1.isEmpty() || str2.isEmpty())
            return Pair(str1, str2)

        val diffChar1 = str1.toCharArray()
        val diffChar2 = str2.toCharArray()

        //获取可比较的最大长度，即较短字符串的长度
        val maxLength = min(diffChar1.size, diffChar2.size)

        //从头开始比较字符，相同置空，不同退出
        for (index in 0 until maxLength) {
            if (diffChar1[index] == diffChar2[index]) {
                diffChar1[index] = ' '
                diffChar2[index] = ' '
            } else {
                break
            }
        }

        //从尾开始比较字符，相同置空，不同退出
        for (index in 1 until maxLength) {
            val charIndex1 = diffChar1.size - index
            val charIndex2 = diffChar2.size - index
            if (diffChar1[charIndex1] == diffChar2[charIndex2]) {
                diffChar1[charIndex1] = ' '
                diffChar2[charIndex2] = ' '
            } else {
                break
            }
        }

        val diffStr1 = String(diffChar1).trim()
        val diffStr2 = String(diffChar2).trim()

        return Pair(diffStr1, diffStr2)
    }

    /**
     * 是否存在需要比较的文件扩展名
     */
    private fun isNeedCompareExtension(str: String): Boolean {
        val separatorIndex = str.indexOf(".")
        return separatorIndex in 1 until str.length
    }
}