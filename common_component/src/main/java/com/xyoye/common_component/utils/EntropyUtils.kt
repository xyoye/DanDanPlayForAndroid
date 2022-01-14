package com.xyoye.common_component.utils

import android.text.TextUtils
import java.security.MessageDigest


/**
 * Created by xyoye on 2022/1/14
 */
object EntropyUtils {

    fun string2Md5(string: String?): String {
        if (TextUtils.isEmpty(string))
            return ""

        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(string!!.toByteArray())
        return buffer2Hex(messageDigest.digest())
    }
}