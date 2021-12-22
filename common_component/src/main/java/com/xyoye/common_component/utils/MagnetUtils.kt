package com.xyoye.common_component.utils

import java.util.*

/**
 * Created by XYJ on 2021/2/15.
 */

object MagnetUtils {

    fun getMagnetHash(magnetLink: String?): String{
        if (magnetLink.isNullOrEmpty())
            return ""

        //头部
        if (!magnetLink.startsWith("magnet:?xt=urn:btih:", true))
            return ""
        var magnet = magnetLink.substring(20)

        //尾部tracker信息
        val extraIndex = magnet.indexOf("&")
         if (extraIndex != -1){
            magnet = magnet.substring(0, extraIndex)
        }

        return when (magnet.length) {
            //SHA1(40位)
            40 -> magnet.uppercase()
            //MD5(32位)
            32 -> magnet.uppercase()
            else -> ""
        }
    }
}