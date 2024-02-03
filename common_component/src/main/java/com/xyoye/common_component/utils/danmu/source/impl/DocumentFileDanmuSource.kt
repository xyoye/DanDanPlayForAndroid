package com.xyoye.common_component.utils.danmu.source.impl

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.danmu.source.AbstractDanmuSource
import java.io.InputStream

/**
 * Created by xyoye on 2024/1/14.
 */

class DocumentFileDanmuSource(
    private val uri: Uri
) : AbstractDanmuSource() {

    override suspend fun getStream(): InputStream? {
        val documentFile = getDocumentFile() ?: return null
        return try {
            getContext().contentResolver.openInputStream(documentFile.uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getDocumentFile(): DocumentFile? {
        val documentFile = DocumentFile.fromSingleUri(getContext(), uri)
            ?: return null
        if (documentFile.exists().not() || documentFile.canRead().not()) {
            return null
        }
        return documentFile
    }

    private fun getContext(): Context {
        return BaseApplication.getAppContext()
    }
}