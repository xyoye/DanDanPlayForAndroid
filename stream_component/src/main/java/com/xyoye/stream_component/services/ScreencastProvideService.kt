package com.xyoye.stream_component.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.utils.screencast.provider.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/14
 *     desc  :
 * </pre>
 */

class ScreencastProvideService : Service() {
    private var httpPort = 20000
    private var httpServer: HttpServer? = null

    private lateinit var notifier: ScreencastProvideNotifier
    private lateinit var receiver: MediaLibraryEntity
    private var ioScope: CoroutineScope? = null

    companion object {
        private const val KEY_SCREENCAST_RECEIVER = "key_screencast_receiver"

        fun start(context: Context, receiver: MediaLibraryEntity) {
            val intent = Intent(context, ScreencastProvideService::class.java)
            intent.putExtra(KEY_SCREENCAST_RECEIVER, receiver)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreencastProvideService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        notifier = ScreencastProvideNotifier(this)

        startForeground(Notifications.Id.SCREENCAST_PROVIDE, notifier.notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val videoSource = VideoSourceManager.getInstance().getSource()
            ?: return super.onStartCommand(intent, flags, startId)
        val receiver: MediaLibraryEntity? = intent.extras?.getParcelable(KEY_SCREENCAST_RECEIVER)
        if (receiver == null) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
        this.receiver = receiver

        if (ioScope == null) {
            ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        }

        stopHttpServer()
        httpServer = createHttpServer(videoSource.getVideoUrl(), httpPort)
        notifier.showProvideVideo(videoSource.getVideoTitle())
        notifyScreencastReceiver(httpServer!!.getProxyUrl())
        return START_STICKY
    }

    override fun onDestroy() {
        stopHttpServer()
        ioScope?.cancel()
        super.onDestroy()
    }

    private fun stopHttpServer() {
        httpServer?.stop()
    }

    private fun createHttpServer(filePath: String, port: Int, retry: Int = 5): HttpServer? {
        return try {
            val httpServer = HttpServer(filePath, port)
            httpServer.start(2000)
            httpServer
        } catch (e: Exception) {
            e.printStackTrace()
            if (retry < 0) {
                createHttpServer(filePath, port + 1, retry - 1)
            } else {
                null
            }
        }
    }

    private fun notifyScreencastReceiver(videoUrl: String) {
        if (ioScope == null) {
            return
        }
        httpRequest<CommonJsonData>(ioScope!!) {
            api {
                Retrofit.screencastService.play(
                    host = receiver.url,
                    port = receiver.port,
                    authorization = receiver.password,
                    params = mapOf("video_url" to videoUrl)
                )
            }

            onSuccess {
                ToastCenter.showError("资源已投屏")
            }

            onError {
                ToastCenter.showError("x${it.code} ${it.msg}")
            }
        }
    }
}