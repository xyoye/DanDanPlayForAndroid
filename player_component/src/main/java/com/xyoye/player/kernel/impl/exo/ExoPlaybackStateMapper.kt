package com.xyoye.player.kernel.impl.exo

import com.google.android.exoplayer2.Player
import com.xyoye.data_component.enums.PlayState

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/30
 *    desc  : 将 EXO 播放状态映射为播放器控制层状态
 */
internal class ExoPlaybackStateMapper {

    // 是否正在等待本次缓冲结束
    private var isBuffering = false

    // 最近一次输出的控制层状态，用于过滤 Surface 重绑产生的重复首帧事件
    private var lastPlayState: PlayState? = null

    /**
     * 根据 EXO 当前状态生成需要通知控制层的播放状态。
     *
     * @param playbackState EXO 当前播放状态
     * @param isPlaying EXO 当前播放位置是否正在推进
     * @return 状态发生变化时返回控制层状态，否则返回 null
     */
    fun map(playbackState: Int, isPlaying: Boolean): PlayState? {
        val playState = when (playbackState) {
            // 缓冲期间播放位置不会推进，控制层和弹幕均保持暂停
            Player.STATE_BUFFERING -> {
                isBuffering = true
                PlayState.STATE_BUFFERING_PAUSED
            }

            // 就绪后按照 EXO 实际播放状态恢复播放或保持暂停
            Player.STATE_READY -> {
                val resumedFromBuffering = isBuffering
                isBuffering = false
                when {
                    // 暂停状态下即使重新渲染首帧，也不能通知控制层继续播放
                    !isPlaying -> PlayState.STATE_PAUSED
                    // 缓冲恢复后沿用原有的缓冲结束状态
                    resumedFromBuffering -> PlayState.STATE_BUFFERING_PLAYING
                    // 非缓冲场景正常进入播放状态
                    else -> PlayState.STATE_PLAYING
                }
            }

            // 初始化和播放结束由现有独立流程处理
            else -> return null
        }

        // Surface 重绑可能重复输出相同状态，无需重复驱动控制层组件
        if (playState == lastPlayState) {
            return null
        }
        lastPlayState = playState
        return playState
    }

    /**
     * 清理上一媒体的状态映射记录。
     */
    fun reset() {
        isBuffering = false
        lastPlayState = null
    }
}
