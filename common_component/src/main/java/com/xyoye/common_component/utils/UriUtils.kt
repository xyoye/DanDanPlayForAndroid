package com.xyoye.common_component.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Created by xyoye on 2021/4/8.
 */
object UriUtils {

    fun queryVideoTitle(context: Context, videoUri: Uri?): String? {
        if (videoUri == null) return null
        val documentFile = DocumentFile.fromSingleUri(context, videoUri) ?: return null
        return documentFile.name
    }
}