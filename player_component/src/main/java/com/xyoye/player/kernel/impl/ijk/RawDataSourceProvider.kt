/*
Copyright 2017 yangchong211（github.com/yangchong211）

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.xyoye.player.kernel.impl.ijk

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.net.Uri
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class RawDataSourceProvider(private var mDescriptor: AssetFileDescriptor?) : IMediaDataSource {
    private var mMediaBytes: ByteArray? = null

    override fun readAt(
        position: Long,
        buffer: ByteArray,
        offset: Int,
        size: Int
    ): Int {
        mMediaBytes?.let {
            if (position + 1 >= it.size) {
                return -1
            }
            var length: Int
            if (position + size < it.size) {
                length = size
            } else {
                length = (it.size - position).toInt()
                if (length > buffer.size) length = buffer.size
                length--
            }
            System.arraycopy(it, position.toInt(), buffer, offset, length)
            return length
        }
        return 0
    }

    @Throws(IOException::class)
    override fun getSize(): Long {
        mDescriptor?.let {
            if (mMediaBytes == null) {
                val inputStream: InputStream = it.createInputStream()
                mMediaBytes = readBytes(inputStream)
            }
            return it.length
        }
        return 0
    }

    @Throws(IOException::class)
    override fun close() {
        mDescriptor?.close()
        mMediaBytes = null
        mDescriptor = null
    }

    @Throws(IOException::class)
    private fun readBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    companion object {
        fun create(context: Context, uri: Uri): RawDataSourceProvider? {
            try {
                val fileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")
                return RawDataSourceProvider(fileDescriptor)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return null
        }
    }

}