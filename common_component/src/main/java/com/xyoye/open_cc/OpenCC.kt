package com.xyoye.open_cc

import android.util.Log
import com.google.common.base.Utf8
import java.io.File

/**
 * Created by xyoye on 2023/5/27
 */

object OpenCC {

    init {
        System.loadLibrary("open_cc")
    }

    private external fun convert(text: String, configJsonPath: String): String

    fun convertSC(text: String): String {
        val config = OpenCCFile.t2s
        if (config.exists().not()) {
            return text
        }

        return convert(text, config)
    }

    fun convertTC(text: String): String {
        val config = OpenCCFile.s2t
        if (config.exists().not()) {
            return text
        }

        return convert(text, config)
    }

    private fun convert(text: String, config: File): String {
        return try {
            // 检查是否是符合 Unicode 6.0 的格式的UFT-8字符串
            // Bugly#2456345
            if (Utf8.isWellFormed(text.toByteArray()).not()) {
                Log.d("OpenCC", "invalid utf-8 string: $text")
                return text
            }

            convert(text, config.absolutePath)
        } catch (t: Throwable) {
            t.printStackTrace()
            text
        }
    }
}