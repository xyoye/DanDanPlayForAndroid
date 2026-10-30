package com.xyoye.player.kernel.impl.vlc

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.view.Surface
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.SurfaceType
import com.xyoye.data_component.enums.TrackType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.kernel.inter.AbstractVideoPlayer
import com.xyoye.player.utils.PlayerConstant
import com.xyoye.player.utils.VideoLog
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.interfaces.IMedia.VideoTrack
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

/**
 * Created by xyoye on 2021/4/12.
 */

class VlcVideoPlayer(private val mContext: Context) : AbstractVideoPlayer() {

    companion object {
        private val TAG = VlcVideoPlayer::class.java.simpleName
    }

    private lateinit var libVlc: LibVLC
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var mediaFactory: VlcMediaFactory

    // 是否已经开始释放播放器资源
    private var isReleased = false

    // 协调 Surface 重建后的播放器状态恢复
    private val renderRestoreCoordinator = VlcRenderRestoreCoordinator()

    // 依次释放 Surface 重建过程中产生的旧播放器
    private val playerReleaseQueue = VlcPlayerReleaseQueue()

    // 当前媒体源及其文件描述符所有权
    private var mediaSourceState: VlcMediaSourceState? = null

    // 当前媒体已添加的外部音轨和字幕，替换 MediaPlayer 后需要重新附加
    private val externalSlaves = mutableListOf<VlcExternalSlave>()

    // 当前媒体已确认的完整时长，重建 MediaPlayer 时保持不变
    private var mCurrentDuration = 0L

    // 控制层确认的播放位置，由 TimeChanged、主动跳转和恢复流程共同维护
    private var lastKnownPositionMs = 0L

    // 媒体重开后等待底层确认的位置，确认前忽略旧输入线程产生的过渡事件
    private var positionRestoreGuardMs = 0L

    private var seekable = true
    private var isBufferEnd = false
    private val mVideoSize = Point(0, 0)

    // 调用方期望的播放状态，用于覆盖 Opening 和 Buffering 阶段 isPlaying 不稳定的情况
    private var playWhenReady = false

    // 当前视频缩放方式，替换 MediaPlayer 后需要重新应用
    private var currentVideoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT

    override fun initPlayer() {
        isReleased = false
        setOptions()
        mediaFactory = VlcMediaFactory(mContext, libVlc)
        mMediaPlayer = createMediaPlayer()
        initVLCEventListener()
    }

    override fun setDataSource(path: String, headers: Map<String, String>?) {
        val mediaHandle = mediaFactory.create(path, headers)
        // 数据源无法打开时保持播放器未加载状态，并通知上层处理错误
        if (mediaHandle == null) {
            mPlayerEventListener.onInfo(PlayerConstant.MEDIA_INFO_URL_EMPTY, 0)
            return
        }

        val previousSourceFd = mediaSourceState?.sourceFd
        mediaSourceState = VlcMediaSourceState(
            path = path,
            headers = headers?.toMap(),
            sourceFd = mediaHandle.sourceFd
        )
        externalSlaves.clear()
        resetMediaState(mediaHandle.media.duration)
        mMediaPlayer.media = mediaHandle.media
        mediaHandle.media.release()

        // 新 Media 接管数据源后，旧文件描述符不再由当前播放器持有
        if (previousSourceFd !== mediaHandle.sourceFd) {
            IOUtils.closeIO(previousSourceFd)
        }
    }

    override fun setSurface(surface: Surface) {

    }

    override fun prepareAsync() {
        playWhenReady = true
        mMediaPlayer.play()
    }

    override fun start() {
        playWhenReady = true
        // 用户在恢复完成前主动播放时，取消恢复后的暂停意图
        if (renderRestoreCoordinator.isPending) {
            renderRestoreCoordinator.updatePlayWhenReady(true)
        }
        mMediaPlayer.play()
    }

    override fun pause() {
        playWhenReady = false
        // 用户在恢复完成前主动暂停时，保留恢复后的暂停意图
        if (renderRestoreCoordinator.isPending) {
            renderRestoreCoordinator.updatePlayWhenReady(false)
        }
        mMediaPlayer.pause()
    }

    override fun stop() {
        playWhenReady = false
        renderRestoreCoordinator.cancel()
        positionRestoreGuardMs = 0L

        if (mMediaPlayer.hasMedia() && !mMediaPlayer.isReleased) {
            mMediaPlayer.stop()
        }
    }

    override fun reset() {

    }

    override fun release() {
        // 异步释放完成前底层 isReleased 仍为 false，需要由播放器层阻止重复释放
        if (isReleased || !this::mMediaPlayer.isInitialized) {
            return
        }
        isReleased = true
        playWhenReady = false
        renderRestoreCoordinator.cancel()
        positionRestoreGuardMs = 0L
        val currentVideoSourceFd = mediaSourceState?.sourceFd
        mediaSourceState = null
        externalSlaves.clear()
        val mediaPlayer = mMediaPlayer
        mediaPlayer.setEventListener(null)
        detachRenderView()
        mediaPlayer.media?.apply {
            setEventListener(null)
            release()
        }
        // 所有旧 MediaPlayer 都持有 LibVLC 引用，必须依次释放后再释放内核
        playerReleaseQueue.enqueueFinal(mediaPlayer, currentVideoSourceFd, libVlc)
    }

    override fun seekTo(timeMs: Long) {
        if (seekable && isPlayerAvailable()) {
            // 用户主动跳转应立即取代 Surface 恢复位置保护
            positionRestoreGuardMs = 0L
            lastKnownPositionMs = timeMs.coerceAtLeast(0L)
            VideoLog.d("$TAG--seek--> position=$lastKnownPositionMs")
            mMediaPlayer.time = timeMs
        }
    }

    override fun setSpeed(speed: Float) {
        mMediaPlayer.rate = speed
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        val volume = ((leftVolume + rightVolume) / 2 * 100).toInt()
        mMediaPlayer.volume = volume
    }

    override fun setLooping(isLooping: Boolean) {

    }

    override fun setOptions() {
        val options = arrayListOf<String>()
        options.add("-vvv")
        options.add("--android-display-chroma")
        options.add(PlayerInitializer.Player.vlcPixelFormat.value)
        libVlc = LibVLC(mContext, options)
    }

    override fun setSubtitleOffset(offsetMs: Long) {
        mMediaPlayer.spuDelay = offsetMs * 1000
    }

    override fun isPlaying(): Boolean {
        return mMediaPlayer.isPlaying && isBufferEnd
    }

    override fun getCurrentPosition(): Long {
        return lastKnownPositionMs
    }

    override fun getDuration(): Long {
        return mCurrentDuration
    }

    override fun getSpeed(): Float {
        return mMediaPlayer.rate
    }

    override fun getVideoSize(): Point {
        return mVideoSize
    }

    override fun getBufferedPercentage(): Int {
        return 0
    }

    override fun getTcpSpeed(): Long {
        return 0
    }

    override fun getTracks(type: TrackType): List<VideoTrackBean> {
        return getVlcTrackType(type)
            ?.let {
                mMediaPlayer.getTracks(it)
            }?.map {
                VideoTrackBean.internal(it.id, it.name, type, selected = it.selected)
            } ?: emptyList()
    }

    override fun selectTrack(track: VideoTrackBean) {
        if (isPlayerAvailable() && track.id.isNullOrEmpty().not()) {
            mMediaPlayer.selectTrack(track.id)
            seekTo(mMediaPlayer.time)
        }
    }

    override fun deselectTrack(type: TrackType) {
        getVlcTrackType(type)?.let {
            mMediaPlayer.unselectTrackType(it)
            seekTo(mMediaPlayer.time)
        }
    }

    override fun supportAddTrack(type: TrackType): Boolean {
        return type == TrackType.SUBTITLE || type == TrackType.AUDIO
    }

    override fun addTrack(track: VideoTrackBean): Boolean {
        val slave = when (track.type) {
            TrackType.AUDIO -> {
                val audioPath = track.type.getAudio(track.trackResource) ?: return false
                VlcExternalSlave(IMedia.Slave.Type.Audio, audioPath)
            }

            TrackType.SUBTITLE -> {
                val subtitlePath = track.type.getSubtitle(track.trackResource) ?: return false
                VlcExternalSlave(IMedia.Slave.Type.Subtitle, subtitlePath)
            }

            else -> return false
        }
        val isAdded = mMediaPlayer.addSlave(slave.type, slave.path, true)
        // 只保存成功添加的外部轨道，避免恢复时重复无效请求
        if (isAdded && slave !in externalSlaves) {
            externalSlaves.add(slave)
        }
        return isAdded
    }

    private fun getVlcTrackType(type: TrackType): Int? {
        return when (type) {
            TrackType.AUDIO -> IMedia.Track.Type.Audio
            TrackType.SUBTITLE -> IMedia.Track.Type.Text
            else -> null
        }
    }

    fun attachRenderView(vlcVideoLayout: VLCVideoLayout) {
        // 播放器已释放时，不再接受界面生命周期触发的渲染绑定
        if (isReleased || !this::mMediaPlayer.isInitialized || mMediaPlayer.isReleased) {
            return
        }
        // Surface 销毁只会解绑 VLCVout，这里需要同时清理 MediaPlayer 持有的旧 VideoHelper
        mMediaPlayer.detachViews()
        val isTextureView = PlayerInitializer.surfaceType == SurfaceType.VIEW_TEXTURE
        mMediaPlayer.attachViews(vlcVideoLayout, null, true, isTextureView)
        mMediaPlayer.videoScale = currentVideoScale

        // 替换后的播放器需要在新 Surface 绑定完成后启动，随后恢复原进度和播放状态
        if (renderRestoreCoordinator.isPending) {
            mMediaPlayer.play()
        }
    }

    /**
     * 停止使用旧 Surface 的视频输出，并记录重新绑定后需要恢复的播放状态。
     *
     * @param keepPlaying 是否需要在解绑 Surface 后继续后台播放音频
     */
    fun prepareRenderViewReattach(keepPlaying: Boolean = false) {
        // 播放器尚未初始化或已经释放时，无需处理渲染载体
        if (isReleased || !this::mMediaPlayer.isInitialized || mMediaPlayer.isReleased) {
            return
        }

        // 已保存恢复状态时只需确保视图保持解绑，避免重复替换覆盖原播放位置
        if (renderRestoreCoordinator.isPending) {
            mMediaPlayer.detachViews()
            return
        }

        // 尚未设置媒体时没有视频输出，只需要解除初始渲染绑定
        if (!mMediaPlayer.hasMedia()) {
            mMediaPlayer.detachViews()
            return
        }

        // 通过统一位置读取逻辑保存进度，避免连续重建时采集到底层过渡值
        val restorePositionMs = getCurrentPosition().coerceAtLeast(0L)
        lastKnownPositionMs = restorePositionMs
        positionRestoreGuardMs = restorePositionMs
        val audioTrackId = mMediaPlayer
            .getSelectedTrack(IMedia.Track.Type.Audio)
            ?.id
        val subtitleTrackId = mMediaPlayer
            .getSelectedTrack(IMedia.Track.Type.Text)
            ?.id
        renderRestoreCoordinator.prepare(
            positionMs = restorePositionMs,
            playWhenReady = playWhenReady,
            audioTrackId = audioTrackId,
            subtitleTrackId = subtitleTrackId
        )
        VideoLog.d(
            "$TAG--render--prepare--> position=$restorePositionMs, " +
                    "playWhenReady=${renderRestoreCoordinator.playWhenReady}"
        )

        // 数据源重新打开失败后由替换流程通知错误，不再继续启动播放器
        if (!replaceMediaPlayerForRenderRestore()) {
            return
        }

        // 后台播放时以无 Surface 方式恢复音频，回前台后直接绑定新 Surface
        if (keepPlaying && renderRestoreCoordinator.playWhenReady) {
            mMediaPlayer.play()
        }
    }

    /**
     * 完整解除 MediaPlayer 与 VLC 渲染视图的绑定。
     */
    private fun detachRenderView() {
        // 播放器尚未初始化或已经释放时，无需处理渲染载体
        if (!this::mMediaPlayer.isInitialized || mMediaPlayer.isReleased) {
            return
        }
        mMediaPlayer.detachViews()
    }

    fun setScale(scale: MediaPlayer.ScaleType) {
        currentVideoScale = scale
        mMediaPlayer.videoScale = scale
    }

    /**
     * 监听 VLC 播放事件并同步控制层状态。
     */
    private fun initVLCEventListener() {
        mMediaPlayer.setEventListener {
            //VlcEventLog.log(it)
            when (it.type) {
                //缓冲
                MediaPlayer.Event.Buffering -> handleBuffering(it.buffering)
                //打开中
                MediaPlayer.Event.Opening -> {
                }
                //播放中
                MediaPlayer.Event.Playing -> {
                    onRenderRestoreStateReady(isPausedEvent = false)
                }
                //已暂停
                MediaPlayer.Event.Paused -> {
                    onRenderRestoreStateReady(isPausedEvent = true)
                }
                //是否可跳转
                MediaPlayer.Event.SeekableChanged -> handleSeekableChanged(it.seekable)
                //播放错误
                MediaPlayer.Event.EncounteredError -> handleEncounteredError()
                //时长输出
                MediaPlayer.Event.LengthChanged -> handleLengthChanged(it.lengthChanged)
                //播放位置输出
                MediaPlayer.Event.TimeChanged -> updatePlaybackPosition(it.timeChanged)

                MediaPlayer.Event.ESSelected -> updateSelectedVideoTrack(it.esChangedType)
                //视频输出
                MediaPlayer.Event.Vout -> handleVout(it.voutCount)
                //播放完成
                MediaPlayer.Event.EndReached -> handleEndReached()
            }
        }
    }

    /**
     * 处理 VLC 缓冲状态变化。
     *
     * @param buffering 当前缓冲进度
     */
    private fun handleBuffering(buffering: Float) {
        isBufferEnd = buffering == 100f
        // 无 Surface 的后台播放可在缓冲完成后再次尝试恢复位置
        if (renderRestoreCoordinator.isPending && isBufferEnd) {
            tryCompleteRenderRestore()
        }

        // 用户期望暂停时，底层缓冲事件不能覆盖控制层的暂停状态
        if (shouldKeepPausedState()) {
            VideoLog.d("$TAG--render--ignore--> Buffering while pause is expected")
        } else if (isBufferEnd) {
            mPlayerEventListener.onInfo(PlayerConstant.MEDIA_INFO_BUFFERING_END, 0)
            VideoLog.d("$TAG--listener--onInfo--> MEDIA_INFO_BUFFERING_END")
        } else {
            mPlayerEventListener.onInfo(PlayerConstant.MEDIA_INFO_BUFFERING_START, 0)
            VideoLog.d("$TAG--listener--onInfo--> MEDIA_INFO_BUFFERING_START")
        }
    }

    /**
     * 处理 VLC 跳转能力变化。
     *
     * @param isSeekable 当前媒体是否支持跳转
     */
    private fun handleSeekableChanged(isSeekable: Boolean) {
        seekable = isSeekable
        // 恢复流程必须等待新输入线程确认可跳转
        if (renderRestoreCoordinator.isPending && isSeekable) {
            renderRestoreCoordinator.onSeekableChanged(true)
            tryCompleteRenderRestore()
        }
    }

    /**
     * 处理 VLC 播放错误。
     */
    private fun handleEncounteredError() {
        playWhenReady = false
        renderRestoreCoordinator.cancel()
        positionRestoreGuardMs = 0L
        mPlayerEventListener.onError()
        VideoLog.d("$TAG--listener--onInfo--> onError")
    }

    /**
     * 使用 VLC 上报的有效时长校正完整媒体时长。
     *
     * @param durationMs VLC 上报的媒体时长，单位为毫秒
     */
    private fun handleLengthChanged(durationMs: Long) {
        // 无效时长不能覆盖已经确认的总时长
        if (durationMs <= 0L) {
            VideoLog.d("$TAG--render--ignore--> invalid duration=$durationMs")
            return
        }
        mCurrentDuration = durationMs
    }

    /**
     * 更新当前选中视频轨道的画面尺寸。
     *
     * @param trackType VLC 轨道类型
     */
    private fun updateSelectedVideoTrack(trackType: Int) {
        // 非视频轨道切换不会影响画面尺寸
        if (trackType != IMedia.Track.Type.Video) {
            return
        }
        val track = mMediaPlayer.getSelectedTrack(IMedia.Track.Type.Video)
        (track as? VideoTrack)?.let { videoTrack ->
            mVideoSize.x = videoTrack.width
            mVideoSize.y = videoTrack.height
        }
    }

    /**
     * 处理 VLC 视频输出数量变化。
     *
     * @param voutCount 当前视频输出数量
     */
    private fun handleVout(voutCount: Int) {
        // 没有可用视频输出时无需刷新 Surface 或恢复画面
        if (voutCount <= 0) {
            return
        }
        mMediaPlayer.updateVideoSurfaces()
        if (renderRestoreCoordinator.isPending) {
            renderRestoreCoordinator.onVoutReady()
        }

        // 用户期望暂停时，视频输出事件不能将控制层切换为播放状态
        if (shouldKeepPausedState()) {
            VideoLog.d("$TAG--render--ignore--> Vout while pause is expected")
            // start-paused 不保证发送 Paused，Vout 可作为暂停状态就绪信号
            onRenderRestoreStateReady(isPausedEvent = true)
        } else {
            tryCompleteRenderRestore()
            mPlayerEventListener.onInfo(
                PlayerConstant.MEDIA_INFO_VIDEO_RENDERING_START,
                0
            )
            VideoLog.d("$TAG--listener--onInfo--> MEDIA_INFO_VIDEO_RENDERING_START")
        }
    }

    /**
     * 处理 VLC 播放结束事件。
     */
    private fun handleEndReached() {
        // stop 可能发送 EndReached，Surface 恢复期间不能将其视为自然播放完成
        if (renderRestoreCoordinator.isPending) {
            VideoLog.d("$TAG--render--ignore--> EndReached after stop")
            return
        }
        playWhenReady = false
        mPlayerEventListener.onCompletion()
        VideoLog.d("$TAG--listener--onInfo--> onCompletion")
    }

    private fun isPlayerAvailable(): Boolean {
        return !isReleased && mMediaPlayer.hasMedia() && !mMediaPlayer.isReleased
    }

    /**
     * 记录恢复用播放器已经进入目标播放状态。
     *
     * @param isPausedEvent 当前是否收到暂停状态信号
     */
    private fun onRenderRestoreStateReady(isPausedEvent: Boolean) {
        // 普通播放事件不需要执行 Surface 恢复逻辑
        if (!renderRestoreCoordinator.isPending) {
            return
        }
        renderRestoreCoordinator.onPlaybackStateReady(isPausedEvent)
        tryCompleteRenderRestore()
    }

    /**
     * 在输入线程和视频输出均就绪后完成播放位置恢复。
     */
    private fun tryCompleteRenderRestore() {
        val restoreSession = renderRestoreCoordinator.getReadySession() ?: return
        val restorePositionMs = restoreSession.positionMs
        val restoredPositionMs = if (restorePositionMs > 0L) {
            mMediaPlayer.setTime(restorePositionMs, false)
        } else {
            0L
        }

        // VLC 尚未接受定位时保留恢复状态，等待后续就绪事件再次尝试
        if (restoredPositionMs < 0L) {
            VideoLog.d("$TAG--render--restore-wait--> position=$restorePositionMs")
            return
        }

        VideoLog.d(
            "$TAG--render--restore--> position=$restorePositionMs, " +
                    "restored=$restoredPositionMs, playWhenReady=${restoreSession.playWhenReady}"
        )

        // setTime 使用完整媒体时间轴，控制层与 VLC 共用同一个绝对位置
        if (restorePositionMs > 0L) {
            lastKnownPositionMs = restorePositionMs
            positionRestoreGuardMs = restorePositionMs
            mPlayerEventListener.onPositionRestored(restorePositionMs)
        }

        renderRestoreCoordinator.complete()

        // 完成定位后恢复用户原先选择的音频和字幕轨道
        restoreSession.audioTrackId?.let { mMediaPlayer.selectTrack(it) }
        restoreSession.subtitleTrackId?.let { mMediaPlayer.selectTrack(it) }
    }

    /**
     * 使用同一个 LibVLC 创建新的 MediaPlayer 和 Media，避免复用旧 free vout。
     *
     * @return 成功创建并替换播放器时返回 true
     */
    private fun replaceMediaPlayerForRenderRestore(): Boolean {
        val oldMediaPlayer = mMediaPlayer
        val currentMediaSource = mediaSourceState
        // 缺少原始数据源时无法重新创建 Media，保留旧实例并取消本次恢复
        if (currentMediaSource == null) {
            renderRestoreCoordinator.cancel()
            positionRestoreGuardMs = 0L
            return false
        }
        val volume = oldMediaPlayer.volume
        val rate = oldMediaPlayer.rate
        val audioDelay = oldMediaPlayer.audioDelay
        val subtitleDelay = oldMediaPlayer.spuDelay
        val oldVideoSourceFd = currentMediaSource.sourceFd

        // 官方实现不在主线程 stop，旧实例交由 IO 线程释放，避免 nativeStop 阻塞生命周期
        oldMediaPlayer.setEventListener(null)
        oldMediaPlayer.detachViews()

        val mediaHandle = mediaFactory.create(
            path = currentMediaSource.path,
            headers = currentMediaSource.headers,
            startPaused = !renderRestoreCoordinator.playWhenReady
        )
        // content 数据源重新打开失败时，释放旧实例并通知上层播放失败
        if (mediaHandle == null) {
            renderRestoreCoordinator.cancel()
            positionRestoreGuardMs = 0L
            // 新播放器不再持有旧描述符，交由旧播放器释放任务一并关闭
            mediaSourceState = null
            mMediaPlayer = createMediaPlayer()
            initVLCEventListener()
            playerReleaseQueue.enqueue(oldMediaPlayer, oldVideoSourceFd)
            mPlayerEventListener.onError()
            return false
        }
        val media = mediaHandle.media
        mediaSourceState = currentMediaSource.copy(sourceFd = mediaHandle.sourceFd)
        seekable = false
        // 外部轨道必须在播放前添加到新 Media，确保输入初始化时一并加载
        externalSlaves.forEach { slave ->
            val slaveUri = Uri.fromFile(File(slave.path)).toString()
            media.addSlave(IMedia.Slave(slave.type, 4, slaveUri))
        }

        mMediaPlayer = createMediaPlayer().apply {
            this.media = media
            this.volume = volume
            this.rate = rate
            this.audioDelay = audioDelay
            this.spuDelay = subtitleDelay
        }
        media.release()
        initVLCEventListener()

        // 旧实例的 native vout 可能阻塞释放，沿用官方实现放到 IO 线程处理
        playerReleaseQueue.enqueue(oldMediaPlayer, oldVideoSourceFd)
        return true
    }

    /**
     * 创建并应用当前 VLC 配置的 MediaPlayer。
     *
     * @return 新的 MediaPlayer 实例
     */
    private fun createMediaPlayer(): MediaPlayer {
        return MediaPlayer(libVlc).apply {
            setAudioOutput(PlayerInitializer.Player.vlcAudioOutput.value)
        }
    }

    /**
     * 判断控制层是否应继续保持暂停状态。
     *
     * @return 用户期望暂停时返回 true
     */
    private fun shouldKeepPausedState(): Boolean {
        return !playWhenReady
    }

    /**
     * 清理上一媒体的播放状态，并保留首次播放允许跳转的原有行为。
     *
     * @param durationMs 新媒体已知时长，单位为毫秒
     */
    private fun resetMediaState(durationMs: Long) {
        mCurrentDuration = durationMs.coerceAtLeast(0L)
        lastKnownPositionMs = 0L
        positionRestoreGuardMs = 0L
        seekable = true
        isBufferEnd = false
        mVideoSize.set(0, 0)
    }

    /**
     * 使用 VLC 的 TimeChanged 事件更新控制层播放位置。
     *
     * @param positionMs VLC 上报的播放位置，单位为毫秒
     */
    private fun updatePlaybackPosition(positionMs: Long) {
        val normalizedPositionMs = positionMs.coerceAtLeast(0L)
        val restoreGuardMs = positionRestoreGuardMs

        // 新输入线程定位完成前可能上报 0 或旧位置，不能让它覆盖本次恢复目标
        if (restoreGuardMs > 0L && normalizedPositionMs < restoreGuardMs) {
            return
        }

        // 底层已确认到达恢复位置，后续事件按正常播放进度更新
        if (restoreGuardMs > 0L) {
            positionRestoreGuardMs = 0L
        }
        lastKnownPositionMs = normalizedPositionMs
    }

}
