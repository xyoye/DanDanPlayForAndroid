package com.xyoye.common_component.extension

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.xyoye.common_component.R
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

val Context.notificationManager: NotificationManager
    get() = getSystemService()!!

fun Context.notificationBuilder(
    channelId: String,
    block: (NotificationCompat.Builder.() -> Unit)? = null
): NotificationCompat.Builder {
    val builder = NotificationCompat.Builder(this, channelId)
        .setColor(ContextCompat.getColor(this, R.color.theme))
    if (block != null) {
        builder.block()
    }
    return builder
}

@Suppress("DEPRECATION")
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val className = serviceClass.name
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningServices = manager.getRunningServices(Integer.MAX_VALUE)
    return runningServices.any { it.service.className == className }
}