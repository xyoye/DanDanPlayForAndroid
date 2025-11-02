package com.xyoye.common_component.utils.screencast

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import com.xyoye.data_component.data.screeencast.RemotePlayerStatus
import com.xyoye.data_component.data.screeencast.RemoteControlResult

/**
 * Remote control bridge between the screencast HTTP server and the active player UI.
 * Player-side code registers the current controller, while the server queries/commands through here.
 */
object ScreencastRemoteControlBridge {
    private const val REMOTE_TIMEOUT_MS = 2000L

    private val controllerRef = AtomicReference<ScreencastRemoteControlTarget?>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun attach(target: ScreencastRemoteControlTarget) {
        controllerRef.set(target)
    }

    fun detach(target: ScreencastRemoteControlTarget) {
        controllerRef.compareAndSet(target, null)
    }

    fun getStatus(): RemoteControlResult {
        val target = controllerRef.get() ?: return RemoteControlResult.failure(404, "播放器未运行")
        return executeOnMain { target.provideRemoteStatus()?.let(RemoteControlResult::success)
            ?: RemoteControlResult.failure(503, "暂无播放数据") }
    }

    fun execute(action: String, params: Map<String, String>): RemoteControlResult {
        val target = controllerRef.get() ?: return RemoteControlResult.failure(404, "播放器未运行")
        if (action.isBlank()) {
            return RemoteControlResult.failure(400, "action 参数不能为空")
        }
        return executeOnMain { target.handleRemoteAction(action, params) }
    }

    private fun executeOnMain(block: () -> RemoteControlResult): RemoteControlResult {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return runCatching(block).getOrElse { error ->
                RemoteControlResult.failure(500, error.message ?: "执行失败")
            }
        }

        val latch = CountDownLatch(1)
        var result: RemoteControlResult = RemoteControlResult.failure(500, "执行失败")
        mainHandler.post {
            result = runCatching(block).getOrElse { error ->
                RemoteControlResult.failure(500, error.message ?: "执行失败")
            }
            latch.countDown()
        }
        val finished = latch.await(REMOTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        if (finished.not()) {
            return RemoteControlResult.failure(504, "执行超时")
        }
        return result
    }
}

interface ScreencastRemoteControlTarget {
    fun provideRemoteStatus(): RemotePlayerStatus?
    fun handleRemoteAction(action: String, params: Map<String, String>): RemoteControlResult
}
