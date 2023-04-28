package com.xyoye.common_component.extension

import android.database.Cursor
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.IOUtils

/**
 * Created by xyoye on 2022/12/30
 */

/**
 * 获取文件名与文件类型
 */
fun DocumentFile.fileNameAndMineType(): Pair<String, String> {
    val contentResolver = BaseApplication.getAppContext().contentResolver

    val queryColumn = arrayOf(
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE
    )

    var cursor: Cursor? = null
    var fileName = ""
    var mimeType = ""

    try {
        cursor = contentResolver.query(uri, queryColumn, null, null, null)
        if (cursor?.moveToFirst() == true) {
            fileName = cursor.getString(0) ?: ""
            mimeType = cursor.getString(1) ?: ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        IOUtils.closeIO(cursor)
    }

    return fileName to mimeType
}