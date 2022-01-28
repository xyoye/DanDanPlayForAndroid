package com.xyoye.common_component.utils.seven_zip

import com.xyoye.common_component.utils.getFileNameNoExtension
import kotlinx.coroutines.suspendCancellableCoroutine
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*
import kotlin.coroutines.resume


/**
 * Created by xyoye on 2020/12/9.
 */

object SevenZipUtils {

    fun getArchiveFormat(fileExtension: String): ArchiveFormat? {
        if (fileExtension.isEmpty()) return null
        for (format in ArchiveFormat.values()) {
            val upperMethodName = format.methodName.uppercase(Locale.ROOT)
            val upperExtension = fileExtension.uppercase(Locale.ROOT)
            if (upperMethodName == upperExtension) {
                return format
            }
        }
        return null
    }


    @Throws(IOException::class)
    suspend fun extractFile(rarFile: File): String? {
        if (!rarFile.exists() || !rarFile.isFile) throw IOException("compress file not found")

        val destDirName: String = getFileNameNoExtension(rarFile.absolutePath)
        val destDir = File(rarFile.parent, destDirName)
        return if (!destDir.exists()) {
            if (destDir.mkdir()) {
                extractFile(rarFile, destDir)
            } else {
                throw IOException("mkdir output directory failed")
            }
        } else {
            extractFile(rarFile, destDir)
        }
    }

    suspend fun extractFile(compressFile: File, destDir: File) =
        suspendCancellableCoroutine<String?> { continuation ->
            if (!compressFile.exists() || !compressFile.isFile) throw IOException("compress file not found")
            if (!destDir.exists() || !destDir.isDirectory) throw IOException("Dest directory not found")

            val randomAccessFile = RandomAccessFile(compressFile, "r")
            val accessFileInStream = RandomAccessFileInStream(randomAccessFile)

            val inArchive = SevenZip.openInArchive(null, accessFileInStream)
            val extractCallback = ArchiveExtractCallback(inArchive, destDir) {
                continuation.resume(it)
            }
            inArchive.extract(null, false, extractCallback)
        }
}