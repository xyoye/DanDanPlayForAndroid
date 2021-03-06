package com.xyoye.common_component.utils.seven_zip

import com.xyoye.common_component.utils.getFileName
import net.sf.sevenzipjbinding.*
import java.io.File

class ArchiveExtractCallback constructor(
    private val inArchive: IInArchive,
    private val destDir: File,
    private val callback: (destDirPath: String) -> Unit
) : IArchiveExtractCallback {

    private var totalProgress: Long = 0
    private var isCompleted = false

    @Throws(SevenZipException::class)
    override fun getStream(index: Int, extractAskMode: ExtractAskMode?): ISequentialOutStream {
        val fileName: String =
            getFileName(inArchive.getProperty(index, PropID.PATH) as String)
        return SequentialOutStream(destDir, fileName)
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode?) {}

    override fun setOperationResult(extractOperationResult: ExtractOperationResult) {
        if (extractOperationResult !== ExtractOperationResult.OK) {
            callback.invoke("")
        }
    }

    override fun setTotal(total: Long) {
        totalProgress = total
    }

    override fun setCompleted(complete: Long) {
        if (complete == totalProgress) {
            if (!isCompleted) {
                isCompleted = true
                callback.invoke(destDir.absolutePath)
            }
        }
    }
}