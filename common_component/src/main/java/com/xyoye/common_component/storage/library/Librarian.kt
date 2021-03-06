package com.xyoye.common_component.storage.library

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import com.xyoye.common_component.utils.DDLog

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

class Librarian private constructor() {

    private val handler: Handler

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = Librarian()
    }

    init {
        val handlerThread = HandlerThread(
            "Librarian::handler",
            Process.THREAD_PRIORITY_BACKGROUND
        )
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    fun safePost(runnable: Runnable) {
        if (Thread.currentThread() === handler.looper.thread) {
            try {
                runnable.run()
            } catch (t: Throwable) {
                DDLog.e("safePost() " + t.message, t)
            }
        } else {
            handler.post {
                try {
                    runnable.run()
                } catch (t: Throwable) {
                    DDLog.e("safePost() " + t.message, t)
                }
            }
        }
    }

    fun shutdownHandler() {
        handler.removeCallbacksAndMessages(null)
    }
}