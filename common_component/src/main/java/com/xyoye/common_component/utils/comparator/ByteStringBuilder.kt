package com.xyoye.common_component.utils.comparator


class ByteStringBuilder(capacity: Int = 16) {
    private var bytes = ByteArray(capacity)

    var length = 0
        private set

    operator fun get(index: Int): Byte {
        if (index !in 0 until length) {
            throw IndexOutOfBoundsException()
        }
        return bytes[index]
    }

    val isEmpty: Boolean
        get() = length == 0

    fun append(byte: Byte): ByteStringBuilder {
        ensureCapacity(length + 1)
        bytes[length] = byte
        ++length
        return this
    }

    fun append(bytes: ByteArray, start: Int = 0, end: Int = bytes.size): ByteStringBuilder {
        val newLength = length + (end - start)
        ensureCapacity(newLength)
        bytes.copyInto(this.bytes, length, start, end)
        length = newLength
        return this
    }

    fun append(byteString: ByteString): ByteStringBuilder = append(byteString.borrowBytes())

    private fun ensureCapacity(minimumCapacity: Int) {
        val capacity = bytes.size
        if (minimumCapacity > capacity) {
            var newCapacity = (capacity shl 1) + 2
            if (newCapacity < minimumCapacity) {
                newCapacity = minimumCapacity
            }
            bytes = bytes.copyOf(newCapacity)
        }
    }

    fun toByteString(): ByteString = bytes.toByteString(0, length)

    override fun toString(): String = String(bytes, 0, length)
}