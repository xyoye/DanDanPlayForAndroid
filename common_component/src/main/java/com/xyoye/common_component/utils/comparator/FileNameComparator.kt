package com.xyoye.common_component.utils.comparator

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.CollationKey
import java.text.Collator
import java.util.Comparator
import kotlin.math.min

/**
 * Created by xyoye on 2023/1/23.
 */

private val COLLATION_SENTINEL = byteArrayOf(1, 1, 1)

class FileNameComparator<T>(
    private val getName: (T) -> String,
    private val isDirectory: (T) -> Boolean,
    private val asc: Boolean = true,
    private val directoryFirst: Boolean = true
) : Comparator<T> {

    override fun compare(o1: T?, o2: T?): Int {
        if (o1 == null) {
            return if (asc) -1 else 1
        }
        if (o2 == null) {
            return if (asc) 1 else -1
        }

        val isDirectory1 = directory(o1, o2, true)
        val isDirectory2 = directory(o1, o2, false)

        return when {
            isDirectory1 == isDirectory2 -> compareFileName(o1, o2)
            isDirectory1 -> -1
            else -> 1
        }
    }

    private fun compareFileName(o1: T, o2: T): Int {
        val key1 = nameKey(o1, o2, true)
        val key2 = nameKey(o1, o2, false)
        return key1.compareTo(key2)
    }

    private fun nameKey(o1: T, o2: T, first: Boolean): CollationKey {
        return if (first) {
            if (asc) o1 else o2
        } else {
            if (asc) o2 else o1
        }.run(getName).run {
            Collator.getInstance().getCollateKey(this)
        }
    }

    private fun directory(o1: T, o2: T, first: Boolean): Boolean {
        return if (first) {
            if (directoryFirst) o1 else o2
        } else {
            if (directoryFirst) o2 else o1
        }.run(isDirectory)
    }
}

@Parcelize
private class ByteArrayCollationKey(
    @Suppress("CanBeParameter")
    private val source: String,
    private val bytes: ByteArray
) : CollationKey(source), Parcelable {
    override fun compareTo(other: CollationKey): Int {
        other as ByteArrayCollationKey
        return bytes.unsignedCompareTo(other.bytes)
    }

    override fun toByteArray(): ByteArray = bytes.copyOf()
}

// @see https://github.com/GNOME/glib/blob/mainline/glib/gunicollate.c
//      g_utf8_collate_key_for_filename()
fun Collator.getCollateKey(source: String): CollationKey {
    val result = ByteStringBuilder()
    val suffix = ByteStringBuilder()
    var previousIndex = 0
    var index = 0
    val endIndex = source.length
    while (index < endIndex) {
        when {
            source[index] == '.' -> {
                if (previousIndex != index) {
                    val collationKey = getCollationKey(source.substring(previousIndex, index))
                    result.append(collationKey.toByteArray())
                }
                result.append(COLLATION_SENTINEL).append(1)
                previousIndex = index + 1
            }
            source[index].isAsciiDigit() -> {
                if (previousIndex != index) {
                    val collationKey = getCollationKey(source.substring(previousIndex, index))
                    result.append(collationKey.toByteArray())
                }
                result.append(COLLATION_SENTINEL).append(2)
                previousIndex = index
                var leadingZeros: Int
                var digits: Int
                if (source[index] == '0') {
                    leadingZeros = 1
                    digits = 0
                } else {
                    leadingZeros = 0
                    digits = 1
                }
                while (++index < endIndex) {
                    if (source[index] == '0' && digits == 0) {
                        ++leadingZeros
                    } else if (source[index].isAsciiDigit()) {
                        ++digits
                    } else {
                        if (digits == 0) {
                            ++digits
                            --leadingZeros
                        }
                        break
                    }
                }
                while (digits > 1) {
                    result.append(':'.code.toByte())
                    --digits
                }
                if (leadingZeros > 0) {
                    suffix.append(leadingZeros.toByte())
                    previousIndex += leadingZeros
                }
                result.append(source.substring(previousIndex, index).toByteString())
                previousIndex = index
                --index
            }
            else -> {}
        }
        ++index
    }
    if (previousIndex != index) {
        val collationKey = getCollationKey(source.substring(previousIndex, index))
        result.append(collationKey.toByteArray())
    }
    result.append(suffix.toByteString())
    return ByteArrayCollationKey(source, result.toByteString().borrowBytes())
}

private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

private fun ByteArray.unsignedCompareTo(other: ByteArray): Int {
    val size = size
    val otherSize = other.size
    for (index in 0 until min(size, otherSize)) {
        val byte = this[index].toInt() and 0xFF
        val otherByte = other[index].toInt() and 0xFF
        if (byte < otherByte) {
            return -1
        } else if (byte > otherByte) {
            return 1
        }
    }
    return size - otherSize
}