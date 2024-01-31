package com.xyoye.storage_component.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.xyoye.common_component.bridge.ServiceLifecycleBridge
import com.xyoye.common_component.extension.aesEncode
import com.xyoye.common_component.extension.authorizationValue
import com.xyoye.common_component.extension.isServiceRunning
import com.xyoye.common_component.extension.mapByLength
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.ScreencastRepository
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.data.screeencast.ScreencastVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.storage_component.utils.screencast.provider.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        val videoSource = VideoSourceManager.getInstance().getSource() as? StorageVideoSource?
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
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * 连接投屏接收端，并投送视频
     */
    private fun connectReceiver(videoSource: StorageVideoSource) {
        serviceScope.launch(Dispatchers.IO) {
            if (isDeviceRunning().not()) {
                stopSelf()
                ToastCenter.showError("连接至投屏设备失败，请确认投屏设备已启用接收服务")
                return@launch
            }

            stopHttpServer()
            //启动代理服务
            val newHttpServer = createHttpServer(videoSource, httpPort)
            if (newHttpServer == null) {
                stopSelf()
                ToastCenter.showError("启用投屏投送服务失败")
                return@launch
            }
            //通知接收端播放
            postScreencastDevicePlay(newHttpServer.listeningPort, videoSource)

            httpServer = newHttpServer
        }
    }

    private fun createHttpServer(
        videoSource: StorageVideoSource,
        port: Int,
        retry: Int = 5
    ): HttpServer? {
        return try {
            val httpServer = HttpServer(port, videoSource, serviceScope, this)
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
    private suspend fun isDeviceRunning(): Boolean {
        val result = ScreencastRepository.init(
            "http://${receiver.screencastAddress}:${receiver.port}",
            receiver.password?.aesEncode()?.authorizationValue()
        )
        return result.isSuccess
    }

    /**
     * 通知投屏接收端播放视频
     */
    private fun postScreencastDevicePlay(port: Int, videoSource: StorageVideoSource) {
        serviceScope.launch {
            val screencastData = createScreencastData(port, videoSource)
            val result = ScreencastRepository.play(
                "http://${receiver.screencastAddress}:${receiver.port}",
                receiver.password?.aesEncode()?.authorizationValue(),
                screencastData
            )

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            ToastCenter.showSuccess("资源已投屏")
        }
    }

    /**
     * 创建发送到投屏接收端的数据
     */
    private fun createScreencastData(port: Int, videoSource: StorageVideoSource): ScreencastData {
        val videoData = mapByLength(videoSource.getGroupSize()) {
            val storageFile = videoSource.indexStorageFile(it)
            val fileHistory = storageFile.playHistory
            ScreencastVideoData(
                storageFile.fileName(),
                storageFile.uniqueKey(),
                fileHistory?.episodeId,
                fileHistory?.danmuPath,
                fileHistory?.subtitlePath,
                fileHistory?.videoPosition ?: 0L,
                fileHistory?.videoDuration ?: 0L
            )
        }

        return ScreencastData(
            port = port,
            playUniqueKey = videoSource.getStorageFile().uniqueKey(),
            relatedVideos = videoData,
            httpHeader = videoSource.getHttpHeader(),
        )
    }

    override fun onProvideVideo(videoSource: BaseVideoSource) {
        notifier.showProvideVideo(videoSource.getVideoTitle())
    }
}