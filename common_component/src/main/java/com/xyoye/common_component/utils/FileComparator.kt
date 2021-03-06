package com.xyoye.common_component.utils

import androidx.core.text.isDigitsOnly
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

    override fun compare(o1: T, o2: T): Int {
        val isDirectory1 = isDirectory.invoke(o1)
        val isDirectory2 = isDirectory.invoke(o2)

        return when {
            isDirectory1 == isDirectory2 -> {
                compareString(o1, o2)
            }
            isDirectory1 && !isDirectory2 -> -1
            else -> 1
        }
    }

    private fun compareString(o1: T, o2: T): Int {
        val str1 = if(desc) value.invoke(o2) else value.invoke(o1)
        val str2 = if(desc) value.invoke(o1) else value.invoke(o2)
        //文字相同
        if(str1 == str2)
            return 0
        //字符串都是数字，转换为BigInteger比较
        if (str1.isDigitsOnly() && str2.isDigitsOnly()){
            return str1.toBigInteger().compareTo(str2.toBigInteger())
        }
        //提取中间差异部分
        val diff = getDiffText(str1, str2)
        return compareDiff(diff.first, diff.second)
    }

    private fun compareDiff(str1: String, str2: String): Int {
        if (str1.isEmpty()) return -1
        if (str2.isEmpty()) return 1

        //字符串都是数字，转换为BigInteger比较
        if (str1.isDigitsOnly() && str2.isDigitsOnly()){
            return str1.toBigInteger().compareTo(str2.toBigInteger())
        }
        return str1.compareTo(str2)
    }

    private fun getDiffText(str1: String, str2: String): Pair<String, String>{
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
        val diffStr2= String(diffChar2).trim()

        return Pair(diffStr1, diffStr2)
    }
}