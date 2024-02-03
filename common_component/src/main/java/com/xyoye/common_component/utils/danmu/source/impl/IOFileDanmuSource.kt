package com.xyoye.common_component.utils.danmu.source.impl

import com.xyoye.common_component.utils.danmu.source.AbstractDanmuSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by xyoye on 2024/1/14.
 */

class IOFileDanmuSource(
    private val path: String
) : AbstractDanmuSource() {

    override suspend fun getStream(): InputStream? {
        val file = getIOFile() ?: return null
        return try {
            withContext(Dispatchers.IO) {
                FileInputStream(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getIOFile(): File? {
        if (path.isEmpty())
            return null

        val file = File(path)
        if (!file.exists() || file.isDirectory)
            return null

        return file
    }

}