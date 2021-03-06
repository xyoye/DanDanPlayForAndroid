package com.xyoye.common_component.utils.seven_zip

import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.SevenZipException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SequentialOutStream constructor(
    private val destDir: File,
    private var fileName: String
) :
    ISequentialOutStream {

    @Throws(SevenZipException::class)
    override fun write(data: ByteArray?): Int {
        if (data == null || data.isEmpty()) {
            throw SevenZipException("null data")
        }
        if (!destDir.exists() || !destDir.isDirectory) {
            throw SevenZipException("out put directory error")
        }
        if (fileName.isEmpty()) {
            fileName = destDir.name.toString() + "_" + System.currentTimeMillis()
        }
        val outFile = File(destDir, fileName)
        try {
            FileOutputStream(outFile).use { fileOutputStream ->
                fileOutputStream.write(data)
                fileOutputStream.flush()
            }
        } catch (e: IOException) {
            throw SevenZipException("failed to write file: $fileName")
        }
        return data.size
    }
}