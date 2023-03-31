package com.xyoye.common_component.utils.comparator
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.min

@Parcelize
// @see https://youtrack.jetbrains.com/issue/KT-24842
// @Parcelize throws IllegalAccessError if the primary constructor is private.
class ByteString internal constructor(
    private val bytes: ByteArray
) : Comparable<ByteString>, Parcelable {
    val length: Int
        get() = bytes.size

    operator fun get(index: Int): Byte = bytes[index]

    operator fun iterator(): ByteIterator = bytes.iterator()

    val indices: IntRange
        get() = bytes.indices

    fun isEmpty(): Boolean = bytes.isEmpty()

    fun isNotEmpty(): Boolean = bytes.isNotEmpty()

    fun borrowBytes(): ByteArray = bytes

    fun startsWith(prefix: ByteString, startIndex: Int = 0): Boolean {
        if (startIndex !in 0..length - prefix.length) {
            return false
        }
        for (index in prefix.indices) {
            if (this[startIndex + index] != prefix[index]) {
                return false
            }
        }
        return true
    }

    fun indexOf(byte: Byte, fromIndex: Int = 0): Int {
        for (index in fromIndex.coerceAtLeast(0) until length) {
            if (this[index] == byte) {
                return index
            }
        }
        return -1
    }

    fun contains(byte: Byte): Boolean = indexOf(byte) != -1

    fun indexOf(substring: ByteString, fromIndex: Int = 0): Int {
        for (index in fromIndex.coerceAtLeast(0) until length - substring.length) {
            if (startsWith(substring, index)) {
                return index
            }
        }
        return -1
    }

    fun contains(substring: ByteString): Boolean = indexOf(substring) != -1

    fun substring(start: Int, end: Int = length): ByteString {
        val length = length
        if (start < 0 || end > length || start > end) {
            throw IndexOutOfBoundsException()
        }
        if (start == 0 && end == length) {
            return this
        }
        return ByteString(bytes.copyOfRange(start, end))
    }

    fun substring(range: IntRange): ByteString = substring(range.first, range.last + 1)

    operator fun plus(other: ByteString): ByteString {
        if (other.isEmpty()) {
            return this
        }
        return ByteString(bytes + other.bytes)
    }

    fun split(delimiter: ByteString): List<ByteString> {
        require(delimiter.isNotEmpty())
        val result = mutableListOf<ByteString>()
        var start = 0
        while (true) {
            val end = indexOf(delimiter, start)
            if (end == -1) {
                break
            }
            result.add(substring(start, end))
            start = end + delimiter.length
        }
        result.add(substring(start))
        return result
    }

    @IgnoredOnParcel
    private var stringCache: String? = null

    override fun toString(): String {
        // We are okay with the potential race condition here.
        var string = stringCache
        if (string == null) {
            // String() uses replacement char instead of throwing exception.
            string = String(bytes)
            stringCache = string
        }
        return string
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as ByteString
        return bytes contentEquals other.bytes
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun compareTo(other: ByteString): Int = bytes.compareTo(other.bytes)

    private fun ByteArray.compareTo(other: ByteArray): Int {
        val size = size
        val otherSize = other.size
        for (index in 0 until min(size, otherSize)) {
            val byte = this[index]
            val otherByte = other[index]
            val result = byte - otherByte
            if (result != 0) {
                return result
            }
        }
        return size - otherSize
    }

    companion object {

        fun fromBytes(bytes: ByteArray, start: Int = 0, end: Int = bytes.size): ByteString =
            ByteString(bytes.copyOfRange(start, end))

        fun fromString(string: String): ByteString =
            ByteString(string.toByteArray()).apply { stringCache = string }
    }
}

fun ByteArray.toByteString(start: Int = 0, end: Int = size): ByteString =
    ByteString.fromBytes(this, start, end)

fun String.toByteString(): ByteString = ByteString.fromString(this)

@OptIn(ExperimentalContracts::class)
fun ByteString?.isNullOrEmpty(): Boolean {
    contract { returns(false) implies (this@isNullOrEmpty != null) }
    return this == null || this.isEmpty()
}