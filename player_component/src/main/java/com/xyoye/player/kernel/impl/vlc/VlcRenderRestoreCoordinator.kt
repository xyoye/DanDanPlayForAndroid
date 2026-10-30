package com.xyoye.player.kernel.impl.vlc

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : VLC 渲染载体重建时需要恢复的播放状态
 */
internal data class VlcRenderRestoreSession(
    val positionMs: Long,
    var playWhenReady: Boolean,
    val audioTrackId: String?,
    val subtitleTrackId: String?,
    var isPlaybackStateReady: Boolean = false,
    var isSeekable: Boolean = false,
    var isVoutReady: Boolean = false
)

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : 协调 VLC 渲染载体重建后的状态收集和播放位置恢复时机
 */
internal class VlcRenderRestoreCoordinator {

    // 当前等待完成的渲染恢复会话
    private var session: VlcRenderRestoreSession? = null

    // 是否正在等待完成渲染恢复
    val isPending: Boolean
        get() = session != null

    // 恢复完成后是否需要继续播放
    val playWhenReady: Boolean
        get() = session?.playWhenReady == true

    /**
     * 创建一次渲染恢复会话。
     *
     * @param positionMs 需要恢复的播放位置，单位为毫秒
     * @param playWhenReady 恢复完成后是否需要继续播放
     * @param audioTrackId 需要恢复的音频轨道 ID
     * @param subtitleTrackId 需要恢复的字幕轨道 ID
     */
    fun prepare(
        positionMs: Long,
        playWhenReady: Boolean,
        audioTrackId: String?,
        subtitleTrackId: String?
    ) {
        session = VlcRenderRestoreSession(
            positionMs = positionMs,
            playWhenReady = playWhenReady,
            audioTrackId = audioTrackId,
            subtitleTrackId = subtitleTrackId
        )
    }

    /**
     * 更新恢复完成后的目标播放状态。
     *
     * @param playWhenReady 是否需要继续播放
     */
    fun updatePlayWhenReady(playWhenReady: Boolean) {
        session?.playWhenReady = playWhenReady
    }

    /**
     * 记录恢复用播放器已经进入播放或暂停状态。
     *
     * @param isPausedEvent 当前是否收到暂停事件
     */
    fun onPlaybackStateReady(isPausedEvent: Boolean) {
        val currentSession = session ?: return

        // 播放恢复等待 Playing，暂停恢复等待 Paused
        if (currentSession.playWhenReady == isPausedEvent) {
            return
        }
        currentSession.isPlaybackStateReady = true
    }

    /**
     * 记录恢复用播放器的跳转能力。
     *
     * @param isSeekable 当前媒体是否支持跳转
     */
    fun onSeekableChanged(isSeekable: Boolean) {
        // 恢复流程只在底层明确支持跳转后继续
        if (isSeekable) {
            session?.isSeekable = true
        }
    }

    /**
     * 记录恢复用播放器已经建立视频输出。
     */
    fun onVoutReady() {
        session?.isVoutReady = true
    }

    /**
     * 获取已经满足恢复条件的会话。
     *
     * @return 可以恢复时返回当前会话，否则返回 null
     */
    fun getReadySession(): VlcRenderRestoreSession? {
        val currentSession = session ?: return null

        // 播放状态和输入线程均就绪后才允许定位
        if (!currentSession.isPlaybackStateReady || !currentSession.isSeekable) {
            return null
        }

        // 暂停恢复需要先建立 Vout，确保定位后能刷新目标画面
        if (!currentSession.playWhenReady && !currentSession.isVoutReady) {
            return null
        }
        return currentSession
    }

    /**
     * 结束当前渲染恢复会话。
     */
    fun complete() {
        session = null
    }

    /**
     * 取消当前渲染恢复会话。
     */
    fun cancel() {
        session = null
    }
}
