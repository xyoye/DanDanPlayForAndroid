package com.xyoye.common_component.utils

import android.content.ContentUris
import android.provider.MediaStore
import java.io.*
import java.nio.channels.FileChannel
import java.security.MessageDigest

/**
 * Created by xyoye on 2020/12/29.
 */

object IOUtils {

    private val messageDigest = MessageDigest.getInstance("MD5")

    /**
     * 通过ID获取视频Uri
     */
    fun getVideoUri(id: Long) =
        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)


    /**
     * 通过文件路径获取文件hash值
     */
    fun getFileHash(filePath: String?): String? {
        if (filePath.isNullOrEmpty())
            return null

        val file = File(filePath)
        if (!file.exists() || file.isDirectory)
            return null

        if (file.length() < 16 * 1024 * 1024) {
            return getFileHash(file)
        }

        var randomAccessFile: RandomAccessFile? = null
        var hash: String? = null
        try {
            randomAccessFile = RandomAccessFile(file, "r")
            randomAccessFile.seek(0)
            val byteArray = ByteArray(16 * 1024 * 1024)
            randomAccessFile.read(byteArray)
            messageDigest.update(byteArray)
            hash = buffer2Hex(messageDigest.digest())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            closeIO(randomAccessFile)
        }
        return hash
    }

    /**
     * 通过文件获取文件hash值
     */
    fun getFileHash(file: File): String? {
        if (!file.exists() || file.isDirectory)
            return null

        var inputStream: InputStream? = null
        var fileChannel: FileChannel? = null
        var hash: String? = null
        try {
            inputStream = FileInputStream(file)
            fileChannel = inputStream.channel
            val byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            messageDigest.update(byteBuffer)
            hash = buffer2Hex(messageDigest.digest())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            closeIO(fileChannel)
            closeIO(inputStream)
        }
        return hash
    }

    /**
     * 通过数据流获取文件hash值
     */
    fun getStreamHash(inputStream: InputStream?, close: Boolean = true): String? {
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
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (close){
                closeIO(inputStream)
            }
        }
        return hash
    }

    /**
     * 获取文件夹大小
     */
    fun getDirectorySize(directory: File): Long{
        if (!directory.exists())
            return 0L
        if (directory.isFile)
            return directory.length()

        var totalSize = 0L
        directory.listFiles()?.forEach {
            totalSize += if (it.isDirectory){
                getDirectorySize(it)
            } else {
                it.length()
            }
        }

        return totalSize
    }

    /**
     * 将字节数据写入文件
     */
    fun writeByteData(
        filePath: String,
        byteArray: ByteArray,
        append: Boolean = false
    ): Boolean {
        val file = File(filePath)
        return writeByteData(file, byteArray, append)
    }

    /**
     * 将字节数据写入文件
     */
    fun writeByteData(
        file: File,
        byteArray: ByteArray,
        append: Boolean = false
    ): Boolean {
        var outputStream: OutputStream? = null

        try {
            if (!file.exists()) {
                val parentFile = file.parentFile
                if (parentFile != null) {
                    if (!parentFile.mkdirs() && !parentFile.isDirectory) {
                        return false
                    }
                }

                if (!file.createNewFile())
                    return false
            }
            if (file.isDirectory || !file.canWrite())
                return false

            outputStream = FileOutputStream(file, append)
            outputStream.write(byteArray)
            outputStream.flush()
            closeIO(outputStream)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            closeIO(outputStream)
        }
        return false
    }

    /**
     * 复制文件
     */
    @Throws(IOException::class)
    fun copyFile(srcFile: File, destFile: File) {
        if (!srcFile.exists()) {
            throw FileNotFoundException("Source '$srcFile' does not exist")
        }
        if (srcFile.isDirectory) {
            throw IOException("Source '$srcFile' exists but is a directory")
        }
        if (srcFile.canonicalPath == destFile.canonicalPath) {
            throw IOException("Source '$srcFile' and destination '$destFile' are the same")
        }
        val parentFile = destFile.parentFile
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory) {
                throw IOException("Destination '$parentFile' directory cannot be created")
            }
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw IOException("Destination '$destFile' exists but is read-only")
        }

        if (destFile.exists() && destFile.isDirectory) {
            throw IOException("Destination '$destFile' exists but is a directory")
        }
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null
        try {
            fileInputStream = FileInputStream(srcFile)
            fileOutputStream = FileOutputStream(destFile)
            inputChannel = fileInputStream.channel
            outputChannel = fileOutputStream.channel
            val size = inputChannel.size()
            var pos: Long = 0
            var count: Long
            while (pos < size) {
                count = (size - pos).coerceAtMost(30 * 1024 * 1024)
                pos += outputChannel.transferFrom(inputChannel, pos, count)
            }
        } finally {
            closeIO(outputChannel)
            closeIO(fileOutputStream)
            closeIO(inputChannel)
            closeIO(fileInputStream)
        }
        if (srcFile.length() != destFile.length()) {
            throw IOException("Failed to copy full contents from '$srcFile' to '$destFile'")
        }
    }

    /**
     * 关闭IO流
     */
    fun closeIO(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (e: IOException) {
            // ignore
        }
    }
}

