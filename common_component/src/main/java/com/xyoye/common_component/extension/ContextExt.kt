package com.xyoye.common_component.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.xyoye.common_component.weight.ToastCenter

/**
 * Created by xyoye on 2021/4/1.
 */

fun Context.startUrlActivity(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        url.addToClipboard()
        ToastCenter.showSuccess("链接已复制")
    }
}