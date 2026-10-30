package com.xyoye.player.kernel.impl.vlc

import android.content.res.AssetFileDescriptor
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.SupervisorScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : 在 IO 线程依次释放 VLC MediaPlayer 及其关联资源
 */
internal class VlcPlayerReleaseQueue {

    // 最近一次释放任务，用于保证多个 MediaPlayer 按加入顺序释放
    private var tailJob: Job? = null

    /**
     * 将旧播放器加入异步释放队列，必须在播放器主线程调用。
     *
     * @param mediaPlayer 需要释放的播放器
     * @param sourceFd 播放器独占的文件描述符
     */
    fun enqueue(mediaPlayer: MediaPlayer, sourceFd: AssetFileDescriptor?) {
        val previousJob = tailJob
        tailJob = SupervisorScope.IO.launch {
            previousJob?.join()
            releaseResources(mediaPlayer, sourceFd)
        }
    }

    /**
     * 等待旧播放器释放后释放当前播放器和 LibVLC，必须在播放器主线程调用。
     *
     * @param mediaPlayer 当前播放器
     * @param sourceFd 当前播放器独占的文件描述符
     * @param libVlc 当前播放器使用的 LibVLC
     */
    fun enqueueFinal(
        mediaPlayer: MediaPlayer,
        sourceFd: AssetFileDescriptor?,
        libVlc: LibVLC
    ) {
        val previousJob = tailJob
        tailJob = SupervisorScope.IO.launch {
            previousJob?.join()
            releaseResources(mediaPlayer, sourceFd)
            libVlc.release()
        }
    }

    /**
     * 释放播放器及其独占的文件描述符。
     *
     * MediaPlayer 释放异常不能阻断后续资源释放，异常仍输出到日志供排查。
     *
     * @param mediaPlayer 需要释放的播放器
     * @param sourceFd 播放器独占的文件描述符
     */
    private fun releaseResources(
        mediaPlayer: MediaPlayer,
        sourceFd: AssetFileDescriptor?
    ) {
        runCatching {
            mediaPlayer.release()
        }.onFailure {
            it.printStackTrace()
        }
        IOUtils.closeIO(sourceFd)
    }
}
