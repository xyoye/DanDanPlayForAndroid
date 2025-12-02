package com.xyoye.common_component.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.extension.notificationManager
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.services.ScreencastProvideService
import com.xyoye.common_component.services.ScreencastReceiveService

/**
 * Created by xyoye on 2022/9/14
 */

class NotificationReceiver : BroadcastReceiver() {

    @Autowired
    lateinit var screencastProvideService: ScreencastProvideService
    @Autowired
    lateinit var screencastReceiveService: ScreencastReceiveService

    private object Action {
        const val CANCEL_SCREENCAST_PROVIDE = "com.xyoye.dandanplay.CANCEL_SCREENCAST_PROVIDE"
        const val CANCEL_SCREENCAST_RECEIVE = "com.xyoye.dandanplay.CANCEL_SCREENCAST_RECEIVE"
    }

    companion object {

        /**
         * 关闭投屏投送服务
         */
        fun cancelScreencastProvidePendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = Action.CANCEL_SCREENCAST_PROVIDE
            }

            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, 0, intent, flag)
        }

        /**
         * 关闭投屏接收服务
         */
        fun cancelScreencastReceivePendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = Action.CANCEL_SCREENCAST_RECEIVE
            }

            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, 0, intent, flag)
        }
    }

    init {
        ARouter.getInstance().inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Action.CANCEL_SCREENCAST_PROVIDE -> cancelScreencastProvide(
                context,
                Notifications.Id.SCREENCAST_PROVIDE
            )
            Action.CANCEL_SCREENCAST_RECEIVE -> cancelScreencastReceive(
                context,
                Notifications.Id.SCREENCAST_RECEIVE
            )
        }
    }

    /**
     * 关闭投屏内容提供服务
     */
    private fun cancelScreencastProvide(context: Context, notificationId: Int) {
        screencastProvideService.stopService(context)
        ContextCompat.getMainExecutor(context).execute {
            dismissNotification(context, notificationId)
        }
    }

    /**
     * 关闭投屏内容提供服务
     */
    private fun cancelScreencastReceive(context: Context, notificationId: Int) {
        screencastReceiveService.stopService(context)
        ContextCompat.getMainExecutor(context).execute {
            dismissNotification(context, notificationId)
        }
    }

    /**
     * 关闭通知
     */
    private fun dismissNotification(context: Context, notificationId: Int) {
        context.notificationManager.cancel(notificationId)
    }
}