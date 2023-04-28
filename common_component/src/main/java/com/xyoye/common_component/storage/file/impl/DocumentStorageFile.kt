package com.xyoye.common_component.storage.file.impl

import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.xyoye.common_component.extension.fileNameAndMineType
import com.xyoye.common_component.storage.file.AbstractStorageFile
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.impl.DocumentFileStorage

/**
 * Created by xyoye on 2022/12/29
 */

class DocumentStorageFile(
    private val documentFile: DocumentFile,
    storage: DocumentFileStorage,
    fileNameAndMimeType: Pair<String, String>? = null
) : AbstractStorageFile(storage) {

    //查询文件名与文件类型
    //自定义只查询一次，如果使用DocumentFile的方法，会执行两次查询
    private val mFileNameAndMimeType = fileNameAndMimeType
        ?: documentFile.fileNameAndMineType()

    private val mFileLength = documentFile.length()

    override fun getRealFile(): Any {
        return documentFile
    }

    override fun filePath(): String {
        return documentFile.uri.encodedPath ?: ""
    }

    override fun fileUrl(): String {
        return documentFile.uri.toString()
    }

    override fun storagePath(): String {
        return fileUrl()
    }

    override fun isDirectory(): Boolean {
        return mFileNameAndMimeType.second == DocumentsContract.Document.MIME_TYPE_DIR
    }

    override fun fileName(): String {
        return mFileNameAndMimeType.first
    }

    override fun fileLength(): Long {
        return mFileLength
    }

    override fun canRead(): Boolean {
        return documentFile.canRead()
    }

    override fun clone(): StorageFile {
        return DocumentStorageFile(
            documentFile,
            storage as DocumentFileStorage,
            mFileNameAndMimeType
        ).also { it.playHistory = playHistory }
    }
}