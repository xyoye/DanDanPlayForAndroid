package com.xyoye.common_component.storage.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.storage.StorageManager
import androidx.core.content.ContextCompat
import com.xyoye.common_component.utils.DDLog
import java.io.File
import java.io.IOException
import java.lang.reflect.Array

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

object ExSdCardUtils {
    private val FIXED_SDCARD_PATHS = buildFixedSdCardPaths()

    fun getExtSdCardFolder(context: Context, file: File): String? {
        if (file.absolutePath.contains("/Android/data".toRegex())) {
            return null
        }

        val canonicalPath: String?
        try {
            canonicalPath = file.canonicalPath
        } catch (e: IOException) {
            // ignore
            return null
        }

        return getExiSdCardPaths(context).find { canonicalPath?.contains(it.toRegex()) ?: false }
    }

    private fun getExiSdCardPaths(context: Context): MutableList<String> {
        val resultPaths = arrayListOf<String>()

        val external = context.getExternalFilesDir("external")
        val externals = ContextCompat.getExternalFilesDirs(context, "external")
        for (file in externals) {
            if (file?.equals(external) == true) {
                val index = file.absolutePath.lastIndexOf("/Android/data")
                if (index > 0) {
                    var path = file.absolutePath.substring(0, index)
                    try {
                        path = File(path).canonicalPath
                    } catch (e: IOException) {
                        // Keep non-canonical path.
                    }
                    resultPaths.add(path)
                }
            } else {
                DDLog.w("ext sd card path wrong: ${file.absolutePath}")
            }
        }

        for (fixedSdcardPath in FIXED_SDCARD_PATHS) {
            if (!resultPaths.contains(fixedSdcardPath)) {
                resultPaths.add(fixedSdcardPath)
            }
        }

        return resultPaths
    }

    @SuppressLint("SdCardPath")
    private fun buildFixedSdCardPaths(): MutableList<String> {
        return arrayListOf(
            "/storage/sdcard1", // Motorola Xoom
            "/storage/extsdcard", // Samsung SGS3
            "/storage/sdcard0/external_sdcard", // user request
            "/mnt/extsdcard",
            "/mnt/sdcard/external_sd", // Samsung galaxy family
            "/mnt/external_sd",
            "/mnt/media_rw/sdcard1", // 4.4.2 on CyanogenMod S3
            "/removable/microsd", // Asus transformer prime
            "/mnt/emmc",
            "/storage/external_SD", // LG
            "/storage/ext_sd", // HTC One Max
            "/storage/removable/sdcard1", // Sony Xperia Z1
            "/data/sdext",
            "/data/sdext2",
            "/data/sdext3",
            "/data/sdext4"
        )
    }
}