package com.xyoye.common_component.utils.seven_zip

import com.xyoye.common_component.utils.getFileNameNoExtension
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*


/**
 * Created by xyoye on 2020/12/9.
 */

object SevenZipUtils {

    fun getArchiveFormat(fileExtension: String): ArchiveFormat? {
        if (fileExtension.isEmpty()) return null
        for (format in ArchiveFormat.values()) {
            val upperMethodName = format.methodName.toUpperCase(Locale.ROOT)
            val upperExtension = fileExtension.toUpperCase(Locale.ROOT)
            if (upperMethodName == upperExtension) {
                return format
            }
        }
        return null
    }


    @Throws(IOException::class)
    fun extractFile(rarFile: File, callback: (destDirPath: String) -> Unit) {
        if (!rarFile.exists() || !rarFile.isFile) throw IOException("compress file not found")
        val destDirName: String = getFileNameNoExtension(rarFile.absolutePath)
        val destDir = File(rarFile.parent, destDirName)
        if (!destDir.exists()) {
            if (destDir.mkdir()) {
                extractFile(
                    rarFile,
                    destDir,
                    callback
                )
            } else {
                throw IOException("mkdir output directory failed")
            }
        } else {
            extractFile(
                rarFile,
                destDir,
                callback
            )
        }
    }

    @Throws(IOException::class)
    fun extractFile(
        compressFile: File,
        destDir: File,
        callback: (destDirPath: String) -> Unit
    ) {
        if (!compressFile.exists() || !compressFile.isFile) throw IOException("compress file not found")
        if (!destDir.exists() || !destDir.isDirectory) throw IOException("Dest directory not found")
        val randomAccessFile = RandomAccessFile(compressFile, "r")
        val accessFileInStream =
            RandomAccessFileInStream(randomAccessFile)
        val inArchive = SevenZip.openInArchive(null, accessFileInStream)
        inArchive.extract(null, false,
            ArchiveExtractCallback(
                inArchive,
                destDir,
                callback
            )
        )
    }
}