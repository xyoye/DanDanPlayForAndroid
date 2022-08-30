package com.xyoye.stream_component.ui.activities.screencast.receiver

import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.stream_component.utils.screencast.receiver.HttpServer
import com.xyoye.stream_component.utils.screencast.receiver.UdpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

class ScreencastViewModel : BaseViewModel() {

    var multicastJob: Job? = null
    var httpServer: HttpServer? = null

    fun startServer(password: String? = null) {
        stopServer()
        multicastJob = viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            //启动HTTP服务器
            val port = Random.nextInt(20000, 30000)
            val httpServer = startHttpServer(password, port)
            hideLoading()
            if (httpServer == null) {
                ToastCenter.showError("启动投屏服务器失败")
                return@launch
            }
            //启动UDP组播
            this@ScreencastViewModel.httpServer = httpServer
            UdpServer.startMulticast(
                httpServer.listeningPort,
                needPassword = password.isNullOrEmpty().not()
            )
        }
    }

    fun stopServer() {
        UdpServer.stopMulticast()
        httpServer?.stop()
        multicastJob?.cancel()
    }

    private fun startHttpServer(password: String?, port: Int, retry: Boolean = true): HttpServer? {
        return try {
            val httpServer = HttpServer(password, port)
            httpServer.start(2000)
            httpServer
        } catch (e: Exception) {
            e.printStackTrace()
            if (retry) {
                startHttpServer(password, port, false)
            } else {
                null
            }
        }
    }
}