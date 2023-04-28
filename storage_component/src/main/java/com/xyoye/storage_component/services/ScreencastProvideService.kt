package com.xyoye.storage_component.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.squareup.moshi.JsonDataException
import com.xyoye.common_component.bridge.ServiceLifecycleBridge
import com.xyoye.common_component.extension.isServiceRunning
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.data.screeencast.ScreencastVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.storage_component.utils.screencast.provider.HttpServer
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.coroutines.resume

/**
 * Created by xyoye on 2022/9/14
 */

interface ScreencastProvideHandler {
    fun onProvideVideo(videoSource: BaseVideoSource)
}

class ScreencastProvideService : Service(), ScreencastProvideHandler {
    private var httpPort = 20000
    private var httpServer: HttpServer? = null

    private lateinit var notifier: ScreencastProvideNotifier
    private lateinit var receiver: MediaLibraryEntity
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val KEY_SCREENCAST_RECEIVER = "key_screencast_receiver"

        fun isRunning(context: Context): Boolean {
            return context.isServiceRunning(ScreencastProvideService::class.java)
        }

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val videoSource = VideoSourceManager.getInstance().getSource()
        //必须存在视频资源
        if (videoSource == null) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
        //必须存在投送接收端
        val receiver: MediaLibraryEntity? = intent?.extras?.getParcelable(KEY_SCREENCAST_RECEIVER)
        if (receiver == null) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
        this.receiver = receiver
        ServiceLifecycleBridge.onScreencastProvideLifeChange(receiver)

        //连接投屏接收端，并投送视频
        connectReceiver(videoSource)
        return START_STICKY
    }

    override fun onDestroy() {
        ServiceLifecycleBridge.onScreencastProvideLifeChange(null)
        stopHttpServer()
        ioScope.cancel()
        super.onDestroy()
    }

    /**
     * 连接投屏接收端，并投送视频
     */
    private fun connectReceiver(videoSource: BaseVideoSource) {
        ioScope.launch(Dispatchers.IO) {
            if (isDeviceRunning().not()) {
                stopSelf()
                ToastCenter.showError("连接至投屏设备失败，请确认投屏设备已启用接收服务")
                return@launch
            }

            stopHttpServer()
            //启动代理服务
            httpServer = createHttpServer(videoSource, httpPort)
            if (httpServer == null) {
                stopSelf()
                ToastCenter.showError("启用投屏投送服务失败")
                return@launch
            }
            //设置投屏接收处理回调
            httpServer!!.setScreenProvideHandler(this@ScreencastProvideService)
            //通知接收端播放
            postScreencastDevicePlay(videoSource, httpServer!!.listeningPort)
        }
    }

    private fun createHttpServer(
        videoSource: BaseVideoSource,
        port: Int,
        retry: Int = 5
    ): HttpServer? {
        return try {
            val httpServer = HttpServer(videoSource, port)
            httpServer.start(2000)
            httpServer
        } catch (e: Exception) {
            e.printStackTrace()
            if (retry < 0) {
                createHttpServer(videoSource, port + 1, retry - 1)
            } else {
                null
            }
        }
    }

    private fun stopHttpServer() {
        httpServer?.stop()
    }

    /**
     * 确认接收服务已启动
     */
    private suspend fun isDeviceRunning() = suspendCancellableCoroutine { continuation ->
        httpRequest<CommonJsonData>(ioScope) {
            api {
                Retrofit.screencastService.init(
                    host = receiver.screencastAddress,
                    port = receiver.port,
                    authorization = receiver.password,
                )
            }

            onSuccess {
                continuation.resume(it.success)
            }

            onError {
                continuation.resume(false)
            }
        }
    }

    /**
     * 通知投屏接收端播放视频
     */
    private fun postScreencastDevicePlay(videoSource: BaseVideoSource, port: Int) {
        httpRequest(ioScope) {
            api {
                val screencastData = createScreencastData(videoSource, port)
                val json = JsonHelper.toJson(screencastData)
                    ?: throw JsonDataException("投屏数据异常")
                val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

                Retrofit.screencastService.play(
                    host = receiver.screencastAddress,
                    port = receiver.port,
                    authorization = receiver.password,
                    data = requestBody
                )
            }

            onSuccess {
                ToastCenter.showSuccess("资源已投屏")
            }

            onError {
                ToastCenter.showError("x${it.code} ${it.msg}")
            }
        }
    }

    private fun createScreencastData(
        videoSource: BaseVideoSource,
        port: Int
    ): ScreencastData {
        val videoData = mutableListOf<ScreencastVideoData>()
        for (index in 0 until videoSource.getGroupSize()) {
            val video = ScreencastVideoData(
                videoIndex = index,
                videoTitle = videoSource.indexTitle(index)
            )
            videoData.add(video)
        }

        return ScreencastData(
            port = port,
            mediaType = videoSource.getMediaType().value,
            httpHeader = videoSource.getHttpHeader(),
            videos = videoData,
            playIndex = videoSource.getGroupIndex(),
            uniqueKey = videoSource.getUniqueKey()
        )
    }

    override fun onProvideVideo(videoSource: BaseVideoSource) {
        notifier.showProvideVideo(videoSource.getVideoTitle())
    }
}