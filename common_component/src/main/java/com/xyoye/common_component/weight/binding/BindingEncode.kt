package com.xyoye.common_component.weight.binding

import androidx.databinding.InverseMethod
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Created by xyoye on 2021/1/26.
 */

object BindingEncode {

    @InverseMethod("stringDecode")
    @JvmStatic
    fun stringEncode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }

    @JvmStatic
    fun stringDecode(value: String): String {
        return URLDecoder.decode(value, Charsets.UTF_8.name())
    }
}