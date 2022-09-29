package com.xyoye.stream_component.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.bridge.ServiceLifecycleBridge
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.ScreencastConfig
import com.xyoye.common_component.extension.isServiceRunning
import com.xyoye.common_component.extension.resumeWhenAlive
import com.xyoye.common_component.notification.Notifications
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.ActivityHelper
import com.xyoye.common_component.utils.screencast.ScreencastHandler
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.data_component.data.screeencast.ScreencastVideoData
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.utils.screencast.receiver.HttpServer
import com.xyoye.stream_component.utils.screencast.receiver.UdpServer
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/16
 *     desc  :
 * </pre>
 */

interface ScreencastReceiveHandler {
    fun onReceiveVideo(screencastData: ScreencastData)
}

class ScreencastReceiveService : Service(), ScreencastReceiveHandler {
    private var httpServer: HttpServer? = null
    private lateinit var ioScope: CoroutineScope

    private lateinit var notifier: ScreencastReceiverNotifier
    private lateinit var multicastLock: WifiManager.MulticastLock
    private var multicastJob: Job? = null

    companion object {
        private const val KEY_SERVER_PORT = "key_server_port"
        private const val KEY_SERVER_PASSWORD = "key_server_password"

        fun isRunning(context: Context): Boolean {
            return context.isServiceRunning(ScreencastReceiveService::class.java)
        }

        fun start(context: Context, httpPort: Int, password: String?) {
            val intent = Intent(context, ScreencastReceiveService::class.java)
            intent.putExtra(KEY_SERVER_PORT, httpPort)
            intent.putExtra(KEY_SERVER_PASSWORD, password)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreencastReceiveService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ServiceLifecycleBridge.onScreencastReceiveLifeChange(true)

        val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = manager.createMulticastLock("udp_multicast")
        multicastLock.acquire()

        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        notifier = ScreencastReceiverNotifier(this)
        startForeground(Notifications.Id.SCREENCAST_RECEIVE, notifier.notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val httpPort: Int? = intent?.extras?.getInt(KEY_SERVER_PORT)
        val password: String? = intent?.extras?.getString(KEY_SERVER_PASSWORD)
        //必须设置端口
        if (httpPort == null || httpPort == 0) {
            return super.onStartCommand(intent, flags, startId)
        }
        //已启动则不再启动
        if (httpServer?.isAlive == true) {
            return super.onStartCommand(intent, flags, startId)
        }
        //停止旧服务
        stopHttpServer()
        //创建新服务
        httpServer = createHttpServer(httpPort, password)
        if (httpServer == null) {
            stopSelf()
            return START_STICKY
        }
        //设置投屏接收处理回调
        httpServer!!.setScreenReceiveHandler(this)
        //启动UDP组播
        startMulticast(password)
        return START_STICKY
    }

    override fun onDestroy() {
        multicastLock.release()
        stopHttpServer()
        UdpServer.stopMulticastEmit()
        multicastJob?.cancel()
        ioScope.cancel()
        ServiceLifecycleBridge.onScreencastReceiveLifeChange(false)
        super.onDestroy()
    }

    /**
     * 处理投屏
     */
    override fun onReceiveVideo(screencastData: ScreencastData) {
        ioScope.launch {
            val targetVideo = screencastData.videos.getOrNull(screencastData.playIndex)
                ?: return@launch

            //询问是否接收投屏
            if (considerAcceptScreencast(targetVideo).not()) {
                return@launch
            }

            //投屏源IP地址不能为空
            if (TextUtils.isEmpty(screencastData.ip)) {
                ToastCenter.showError("播放失败，未知的投屏源地址")
                return@launch
            }

            //创建投屏资源
            val videoSource = VideoSourceFactory.Builder()
                .setVideoSources(listOf(screencastData))
                .setIndex(screencastData.playIndex)
                .create(MediaType.SCREEN_CAST)
            if (videoSource == null) {
                ToastCenter.showError("播放失败，无法打开播放资源")
                return@launch
            }

            notifier.showReceivedVideo(targetVideo.videoTitle)

            //正在播放器页面时，不打开新的播放器
            val topActivity = ActivityHelper.instance.getTopActivity()
            if (topActivity != null && topActivity is ScreencastHandler) {
                topActivity.playScreencast(videoSource)
                return@launch
            }

            VideoSourceManager.getInstance().setSource(videoSource)
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
    }

    /**
     * 启动UDP组播
     */
    private fun startMulticast(password: String?) {
        multicastJob?.cancel()
        multicastJob = ioScope.launch {
            UdpServer.startMulticastEmit(
                httpServer!!.listeningPort,
                needPassword = password.isNullOrEmpty().not()
            )
        }
    }

    /**
     * 弹窗确认接收投屏
     */
    private suspend fun considerAcceptScreencast(videoData: ScreencastVideoData): Boolean {
        //设置了自动接收
        if (ScreencastConfig.isReceiveNeedConfirm().not()) {
            return true
        }

        //获取当前正在展示的Activity
        val topActivity = ActivityHelper.instance.getTopActivity()
        if (topActivity == null) {
            ToastCenter.showError("接收到投屏请求，应用处理失败")
            return false
        }

        //展示弹窗询问是否接收投屏
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                AlertDialog.Builder(topActivity)
                    .setTitle("接收到投屏请求，是否播放？")
                    .setMessage("投屏内容：${videoData.videoTitle}")
                    .setNegativeButton("忽略") { dialog, _ ->
                        dialog.dismiss()
                        continuation.resume(false)
                    }
                    .setPositiveButton("播放") { dialog, _ ->
                        dialog.dismiss()
                        continuation.resume(true)
                    }
                    .show()
                    .setOnDismissListener {
                        continuation.resumeWhenAlive(false)
                    }
            }
        }
    }

    private fun stopHttpServer() {
        httpServer?.setScreenReceiveHandler(null)
        httpServer?.stop()
    }

    private fun createHttpServer(port: Int, password: String?): HttpServer? {
        return try {
            val httpServer = HttpServer(password, port)
            httpServer.start(2000)
            httpServer
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}