package com.xyoye.player.kernel.impl.vlc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : VLC 渲染恢复协调器状态转换测试
 */
class VlcRenderRestoreCoordinatorTest {

    /**
     * 验证播放恢复只需要播放状态和跳转能力就绪。
     */
    @Test
    fun playingRestoreRequiresPlaybackStateAndSeekable() {
        val coordinator = createCoordinator(playWhenReady = true)

        coordinator.onPlaybackStateReady(isPausedEvent = false)
        assertNull(coordinator.getReadySession())

        coordinator.onSeekableChanged(true)
        assertNotNull(coordinator.getReadySession())
    }

    /**
     * 验证暂停恢复必须额外等待视频输出建立。
     */
    @Test
    fun pausedRestoreRequiresVout() {
        val coordinator = createCoordinator(playWhenReady = false)

        coordinator.onPlaybackStateReady(isPausedEvent = true)
        coordinator.onSeekableChanged(true)
        assertNull(coordinator.getReadySession())

        coordinator.onVoutReady()
        assertNotNull(coordinator.getReadySession())
    }

    /**
     * 验证不符合目标播放状态的事件不会完成恢复。
     */
    @Test
    fun unmatchedPlaybackStateIsIgnored() {
        val coordinator = createCoordinator(playWhenReady = true)

        coordinator.onPlaybackStateReady(isPausedEvent = true)
        coordinator.onSeekableChanged(true)
        assertNull(coordinator.getReadySession())

        coordinator.onPlaybackStateReady(isPausedEvent = false)
        assertNotNull(coordinator.getReadySession())
    }

    /**
     * 验证更新播放意图后按照新的目标状态完成恢复。
     */
    @Test
    fun updatedPlayWhenReadyChangesTargetState() {
        val coordinator = createCoordinator(playWhenReady = false)

        coordinator.updatePlayWhenReady(true)
        coordinator.onPlaybackStateReady(isPausedEvent = false)
        coordinator.onSeekableChanged(true)

        assertTrue(coordinator.playWhenReady)
        assertNotNull(coordinator.getReadySession())
    }

    /**
     * 验证取消恢复会清理当前会话。
     */
    @Test
    fun cancelClearsPendingSession() {
        val coordinator = createCoordinator(playWhenReady = true)
        assertTrue(coordinator.isPending)

        coordinator.cancel()

        assertFalse(coordinator.isPending)
        assertNull(coordinator.getReadySession())
    }

    /**
     * 创建具有指定目标播放状态的恢复协调器。
     *
     * @param playWhenReady 恢复完成后是否需要继续播放
     * @return 已准备恢复会话的协调器
     */
    private fun createCoordinator(playWhenReady: Boolean): VlcRenderRestoreCoordinator {
        return VlcRenderRestoreCoordinator().apply {
            prepare(
                positionMs = 10_000L,
                playWhenReady = playWhenReady,
                audioTrackId = "audio",
                subtitleTrackId = "subtitle"
            )
        }
    }
}
