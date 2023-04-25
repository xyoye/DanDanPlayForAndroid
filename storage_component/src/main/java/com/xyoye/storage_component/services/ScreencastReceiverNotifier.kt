package com.xyoye.storage_component.services

import android.content.Context
import android.graphics.BitmapFactory
import com.xyoye.common_component.extension.notificationBuilder
import com.xyoye.common_component.extension.notificationManager
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.receiver.NotificationReceiver
import com.xyoye.storage_component.R

/**
 * Created by xyoye on 2022/9/16
 */

class ScreencastReceiverNotifier(private val context: Context) {

    private val cancelIntent by lazy {
        NotificationReceiver.cancelScreencastReceivePendingBroadcast(context)
    }

    private val notificationBitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_logo)
    }

    val notificationBuilder by lazy {
        context.notificationBuilder(Notifications.Channel.SCREENCAST_RECEIVE) {
            setContentTitle("投屏接收服务")
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

    fun showReceivedVideo(name: String) {
        notificationBuilder.setContentText("最近投屏：$name")

        context.notificationManager.notify(
            Notifications.Id.SCREENCAST_RECEIVE,
            notificationBuilder.build(),
        )
    }
}