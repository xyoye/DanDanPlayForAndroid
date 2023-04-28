package com.xyoye.storage_component.services

import android.content.Context
import android.graphics.BitmapFactory
import com.xyoye.common_component.extension.notificationBuilder
import com.xyoye.common_component.extension.notificationManager
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.receiver.NotificationReceiver
import com.xyoye.storage_component.R

/**
 * Created by xyoye on 2022/9/14
 */

class ScreencastProvideNotifier(private val context: Context) {

    private val cancelIntent by lazy {
        NotificationReceiver.cancelScreencastProvidePendingBroadcast(context)
    }

    private val notificationBitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_logo)
    }

    val notificationBuilder by lazy {
        context.notificationBuilder(Notifications.Channel.SCREENCAST_PROVIDE) {
            setContentTitle("投屏内容提供服务")
            setLargeIcon(notificationBitmap)
            setSmallIcon(R.mipmap.ic_logo)
            setOngoing(true)
            setOnlyAlertOnce(true)
            addAction(
                R.drawable.ic_notification_close,
                context.getString(android.R.string.cancel),
                cancelIntent
            )
        }
    }

    fun showProvideVideo(name: String) {
        notificationBuilder.setContentText("投屏中：$name")

        context.notificationManager.notify(
            Notifications.Id.SCREENCAST_PROVIDE,
            notificationBuilder.build(),
        )
    }
}