package com.xyoye.common_component.storage.file_system

import android.os.ParcelFileDescriptor
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.IOUtils
import java.io.*

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

abstract class AbstractFileSystem : FileSystem {

    override fun isDirectory(file: File) = file.isDirectory

    override fun isFile(file: File) = file.isFile

    override fun canWrite(file: File) = file.canWrite()

    override fun length(file: File) = file.length()

    override fun lastModified(file: File) = file.lastModified()

    override fun exists(file: File) = file.exists()

    override fun mkDirs(file: File) = file.mkdirs()

    override fun delete(file: File) = file.delete()

    override fun scan(file: File) {
        // not support
    }

    override fun copy(src: File, dest: File): Boolean {
        try {
            IOUtils.copyFile(src, dest)
            return true
        } catch (t: Throwable) {
            DDLog.e("copy file is failed: $src -> $dest", t)
        }
        return false
    }

    override fun write(file: File, data: ByteArray): Boolean {
        try {
            IOUtils.writeByteData(file, data)
            return true
        } catch (t: Throwable) {
            DDLog.e("writing data to file is failed: $file", t)
        }
        return false
    }

    override fun write(file: File, inputStream: InputStream, notClose: Boolean): Boolean {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(16 * 1024) // MAGIC_NUMBER
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            return true
        } catch (t: Throwable){
            DDLog.e("writing data to file is failed: $file", t)
        } finally {
            if (!notClose){
                IOUtils.closeIO(inputStream)
            }
            IOUtils.closeIO(outputStream)
        }
        return false
    }

    override fun read(file: File): ByteArray? {
        if (!file.exists() || !file.isFile || !file.canRead()){
            return null
        }

        var inputStream: FileInputStream? = null

        try {
            inputStream = FileInputStream(file)
            val byteArray = ByteArray(inputStream.available())
            inputStream.read(byteArray)
            return byteArray
        } catch (t: Throwable){
            DDLog.e("read data failed: $file", t)
        } finally {
            IOUtils.closeIO(inputStream)
        }
        return null
    }

    override fun listFiles(file: File, filter: FileFilter?): Array<File>? {
        return file.listFiles(filter)
    }

    override fun walk(file: File, filter: FileFilter) {
        val childFiles = listFiles(file, filter) ?: return
        for (childFile in childFiles) {
            if (isDirectory(childFile)) {
                walk(childFile, filter)
            } else {
                filter.file(childFile)
            }
        }
    }

    override fun openFd(file: File, mode: String): Int {
        val fdMode = when (mode) {
            "r" -> ParcelFileDescriptor.MODE_READ_ONLY
            "w" -> ParcelFileDescriptor.MODE_WRITE_ONLY
            "rw" -> ParcelFileDescriptor.MODE_READ_WRITE
            else -> {
                DDLog.e("Only r, w or rw modes supported")
                return -1
            }
        }

        if (!file.exists()){
            val parentFile = file.parentFile
            if (parentFile != null) {
                if (!parentFile.mkdirs() && !parentFile.isDirectory) {
                    DDLog.e("Destination '$parentFile' directory cannot be created")
                    return -1
                }
            }
            if (!file.createNewFile()){
                DDLog.e("Destination $file file cannot be created")
                return -1
            }
        }

        return try {
            val fd = ParcelFileDescriptor.open(file, fdMode) ?: return -1
            fd.detachFd()
        } catch (t: Throwable) {
            DDLog.e("Unable to get native fd", t)
            -1
        }
    }
}