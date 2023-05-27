package com.xyoye.open_cc

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
        if (config.exists().not()){
            return text
        }
        return convert(text, config.absolutePath)
    }

    fun convertTC(text: String): String {
        val config = OpenCCFile.s2t
        if (config.exists().not()){
            return text
        }
        return convert(text, config.absolutePath)
    }
}