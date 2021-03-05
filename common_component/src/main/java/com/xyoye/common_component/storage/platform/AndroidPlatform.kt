package com.xyoye.common_component.storage.platform

import android.content.Context
import com.xyoye.common_component.storage.file_system.LollipopFileSystem
import com.xyoye.common_component.storage.utils.ExSdCardUtils
import com.xyoye.common_component.utils.PathHelper
import java.io.File

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

class AndroidPlatform private constructor() {

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance

        fun saf(context: Context, file: File): Boolean {
            if (file.path.contains(PathHelper.PRIVATE_DIRECTORY_PATH)) {
                return false
            }

            return ExSdCardUtils.getExtSdCardFolder(context, file) != null
        }
    }

    private lateinit var fileSystem: LollipopFileSystem

    private object Holder {
        val instance = AndroidPlatform()
    }

    fun init(context: Context) {
        if (this::fileSystem.isInitialized) {
            throw IllegalStateException("Duplicate instantiation is not allowed")
        }
        fileSystem = LollipopFileSystem(context)
    }

    fun getFileSystem(): LollipopFileSystem {
        if (!this::fileSystem.isInitialized) {
            throw IllegalStateException("Wrong call timing, file system uninitialized")
        }
        return fileSystem
    }
}