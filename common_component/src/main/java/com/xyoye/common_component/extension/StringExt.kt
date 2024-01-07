package com.xyoye.common_component.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.EntropyUtils
import com.xyoye.common_component.utils.PathHelper
import java.io.File
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 * Created by xyoye on 2021/3/20.
 */

fun String?.toFile() : File? {
    if (this.isNullOrEmpty())
        return null
    return File(this)
}

fun String?.toCoverFile(): File? {
    if (this.isNullOrEmpty())
        return null
    return File(PathHelper.getVideoCoverDirectory(), this)
}

fun String.addToClipboard(){
    val clipboard = BaseApplication.getAppContext()
        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("data", this)
    clipboard.setPrimaryClip(clipData)
}

fun String.decodeUrl(charset: Charset = Charsets.UTF_8): String{
    if (isNullOrEmpty())
        return this
    return try {
        URLDecoder.decode(this, charset.name())
    } catch (e: Exception){
        this
    }
}

fun String.formatFileName() = trim().replace("[*>/:\\\\?<|]".toRegex(), "_").replace(" ", "_")

fun String?.toMd5String() = EntropyUtils.string2Md5(this)

inline fun String?.ifEmptyOrNull(defaultValue: () -> String): String =
    if (this.isNullOrEmpty()) defaultValue() else this