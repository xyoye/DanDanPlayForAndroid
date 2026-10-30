package com.xyoye.player.kernel.impl.exo

import com.google.android.exoplayer2.Player
import com.xyoye.data_component.enums.PlayState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/30
 *    desc  : EXO 播放状态映射测试
 */
class ExoPlaybackStateMapperTest {

    /**
     * 验证首次就绪且实际播放时进入播放状态。
     */
    @Test
    fun readyAndPlayingMapsToPlaying() {
        val mapper = ExoPlaybackStateMapper()

        val playState = mapper.map(Player.STATE_READY, isPlaying = true)

        assertEquals(PlayState.STATE_PLAYING, playState)
    }

    /**
     * 验证暂停状态下重新渲染首帧仍保持暂停。
     */
    @Test
    fun readyAndPausedMapsToPaused() {
        val mapper = ExoPlaybackStateMapper()

        val playState = mapper.map(Player.STATE_READY, isPlaying = false)

        assertEquals(PlayState.STATE_PAUSED, playState)
    }

    /**
     * 验证缓冲期间控制层保持暂停。
     */
    @Test
    fun bufferingMapsToBufferingPaused() {
        val mapper = ExoPlaybackStateMapper()

        val playState = mapper.map(Player.STATE_BUFFERING, isPlaying = false)

        assertEquals(PlayState.STATE_BUFFERING_PAUSED, playState)
    }

    /**
     * 验证缓冲结束并继续播放时进入缓冲恢复状态。
     */
    @Test
    fun playingAfterBufferingMapsToBufferingPlaying() {
        val mapper = ExoPlaybackStateMapper()
        mapper.map(Player.STATE_BUFFERING, isPlaying = false)

        val playState = mapper.map(Player.STATE_READY, isPlaying = true)

        assertEquals(PlayState.STATE_BUFFERING_PLAYING, playState)
    }

    /**
     * 验证缓冲结束但仍暂停时不会错误恢复播放。
     */
    @Test
    fun pausedAfterBufferingMapsToPaused() {
        val mapper = ExoPlaybackStateMapper()
        mapper.map(Player.STATE_BUFFERING, isPlaying = false)

        val playState = mapper.map(Player.STATE_READY, isPlaying = false)

        assertEquals(PlayState.STATE_PAUSED, playState)
    }

    /**
     * 验证 Surface 重绑产生的重复暂停状态不会再次通知控制层。
     */
    @Test
    fun repeatedPausedStateIsIgnored() {
        val mapper = ExoPlaybackStateMapper()
        mapper.map(Player.STATE_READY, isPlaying = false)

        val playState = mapper.map(Player.STATE_READY, isPlaying = false)

        assertNull(playState)
    }
}
