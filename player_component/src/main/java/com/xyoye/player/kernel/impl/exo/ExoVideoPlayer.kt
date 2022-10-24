package com.xyoye.player.kernel.impl.exo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Surface
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.DefaultAnalyticsCollector
import com.google.android.exoplayer2.ext.FfmpegRenderersFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.video.VideoSize
import com.xyoye.data_component.bean.VideoStreamBean
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.kernel.inter.AbstractVideoPlayer
import com.xyoye.player.utils.PlayerConstant
import com.xyoye.subtitle.MixedSubtitle
import com.xyoye.subtitle.SubtitleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2020/10/29.
 */

class ExoVideoPlayer(private val mContext: Context) : AbstractVideoPlayer(), Player.Listener {
    private lateinit var exoplayer: ExoPlayer
    private lateinit var mMediaSource: MediaSource

    private lateinit var mRenderersFactory: RenderersFactory
    private lateinit var mTrackSelector: TrackSelector
    private lateinit var mLoadControl: LoadControl
    private lateinit var mSpeedPlaybackParameters: PlaybackParameters
    private lateinit var mMediaSourceEventListener: MediaSourceEventListener

    private var subtitleType = SubtitleType.UN_KNOW

    private var mIsPreparing = false
    private var mIsBuffering = false
    private var mLastReportedPlayWhenReady = false
    private var mLastReportedPlaybackState = Player.STATE_IDLE

    override fun initPlayer() {
        if (!this::mRenderersFactory.isInitialized) {
            mRenderersFactory = FfmpegRenderersFactory(mContext)
        }
        if (!this::mTrackSelector.isInitialized) {
            mTrackSelector = DefaultTrackSelector(mContext)
        }
        if (!this::mLoadControl.isInitialized) {
            mLoadControl = DefaultLoadControl()
        }

        //番剧，字幕优先使用中文，音频优先使用日语
        mTrackSelector.parameters = TrackSelectionParameters.Builder(mContext)
            .setPreferredTextLanguage("zh")
            .setPreferredAudioLanguage("jap")
            .build()

        exoplayer = ExoPlayer.Builder(
            mContext,
            mRenderersFactory,
            DefaultMediaSourceFactory(mContext),
            mTrackSelector,
            mLoadControl,
            DefaultBandwidthMeter.getSingletonInstance(mContext),
            DefaultAnalyticsCollector(Clock.DEFAULT)
        ).build()

        setOptions()

        if (PlayerInitializer.isPrintLog && mTrackSelector is MappingTrackSelector) {
            exoplayer.addAnalyticsListener(
                EventLogger(ExoVideoPlayer::class.java.simpleName)
            )
        }

        initListener()
    }

    override fun setDataSource(path: String, headers: Map<String, String>?) {
        if (path.isEmpty()) {
            mPlayerEventListener.onInfo(PlayerConstant.MEDIA_INFO_URL_EMPTY, 0)
            return
        }
        mMediaSource = ExoMediaSourceHelper.getMediaSource(path, headers)
    }

    override fun setSurface(surface: Surface) {
        exoplayer.setVideoSurface(surface)
    }

    override fun prepareAsync() {
        if (!this::mMediaSource.isInitialized) {
            return
        }
        if (this::mSpeedPlaybackParameters.isInitialized) {
            exoplayer.playbackParameters = mSpeedPlaybackParameters
        }

        mIsPreparing = true
        mMediaSource.addEventListener(Handler(Looper.getMainLooper()), mMediaSourceEventListener)
        exoplayer.setMediaSource(mMediaSource)
        exoplayer.prepare()
    }

    override fun start() {
        exoplayer.playWhenReady = true
    }

    override fun pause() {
        exoplayer.playWhenReady = false
    }

    override fun stop() {
        exoplayer.stop()
    }

    override fun reset() {
        exoplayer.stop()
        exoplayer.setVideoSurface(null)
        mIsPreparing = false
        mIsBuffering = false
        mLastReportedPlaybackState = Player.STATE_IDLE
        mLastReportedPlayWhenReady = false
    }

    override fun release() {
        exoplayer.apply {
            removeListener(this@ExoVideoPlayer)
            GlobalScope.launch(Dispatchers.Main) {
                release()
            }
        }

        mIsPreparing = false
        mIsBuffering = false
        mLastReportedPlaybackState = Player.STATE_IDLE
        mLastReportedPlayWhenReady = false
    }

    override fun seekTo(timeMs: Long) {
        exoplayer.seekTo(timeMs)
    }

    override fun setSpeed(speed: Float) {
        mSpeedPlaybackParameters = PlaybackParameters(speed)
        exoplayer.playbackParameters = mSpeedPlaybackParameters
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        exoplayer.volume = (leftVolume + rightVolume) / 2
    }

    override fun setLooping(isLooping: Boolean) {
        exoplayer.repeatMode = if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    override fun setOptions() {
        exoplayer.playWhenReady = true
    }

    //not support
    override fun setSubtitleOffset(offsetMs: Long) {
    }

    override fun isPlaying(): Boolean {
        return when (exoplayer.playbackState) {
            Player.STATE_BUFFERING,
            Player.STATE_READY -> exoplayer.playWhenReady
            Player.STATE_IDLE,
            Player.STATE_ENDED -> false
            else -> false
        }
    }

    override fun getCurrentPosition() = exoplayer.contentPosition

    override fun getDuration() = exoplayer.duration

    override fun getSpeed(): Float {
        return if (this::mSpeedPlaybackParameters.isInitialized) {
            mSpeedPlaybackParameters.speed
        } else {
            1f
        }
    }

    override fun getBufferedPercentage() = exoplayer.bufferedPercentage

    //not support
    override fun getTcpSpeed(): Long = 0L

    override fun getAudioStream(): List<VideoStreamBean> {
        return getStreams(true)
    }

    override fun getSubtitleStream(): List<VideoStreamBean> {
        return getStreams(false)
    }

    override fun selectStream(stream: VideoStreamBean) {
        val streamType = if (stream.isAudio) C.TRACK_TYPE_AUDIO else C.TRACK_TYPE_TEXT
        val mediaTrackGroup = exoplayer.currentTracks
            .groups.getOrNull(stream.trackGroupId)
            ?.mediaTrackGroup
            ?: return
        val override = TrackSelectionOverride(mediaTrackGroup, stream.trackId)

        val trackParams = TrackSelectionParameters.Builder(mContext)
            .setTrackTypeDisabled(streamType, false)
            .clearOverridesOfType(mediaTrackGroup.type)
            .addOverride(override)
            .build()
        mTrackSelector.parameters = trackParams
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        mPlayerEventListener.onVideoSizeChange(videoSize.width, videoSize.height)
        if (videoSize.unappliedRotationDegrees > 0) {
            mPlayerEventListener.onInfo(
                PlayerConstant.MEDIA_INFO_VIDEO_ROTATION_CHANGED,
                videoSize.unappliedRotationDegrees
            )
        }
    }

    override fun onRenderedFirstFrame() {
        mPlayerEventListener.onInfo(PlayerConstant.MEDIA_INFO_VIDEO_RENDERING_START, 0)
        mIsPreparing = false
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (mIsPreparing) {
            return
        }

        val playWhenReady = exoplayer.playWhenReady
        if (mLastReportedPlayWhenReady != playWhenReady || mLastReportedPlaybackState != state) {
            when (state) {
                Player.STATE_IDLE -> {

                }
                Player.STATE_BUFFERING -> {
                    mIsBuffering = true
                    mPlayerEventListener.onInfo(
                        PlayerConstant.MEDIA_INFO_BUFFERING_START,
                        getBufferedPercentage()
                    )
                }
                Player.STATE_READY -> {
                    if (mIsBuffering) {
                        mPlayerEventListener.onInfo(
                            PlayerConstant.MEDIA_INFO_BUFFERING_END,
                            getBufferedPercentage()
                        )
                        mIsBuffering = false
                    }
                }
                Player.STATE_ENDED -> {
                    mPlayerEventListener.onCompletion()
                }
            }
        }
        mLastReportedPlaybackState = state
        mLastReportedPlayWhenReady = playWhenReady
    }

    override fun onTracksChanged(tracks: Tracks) {
        subtitleType = SubtitleType.UN_KNOW
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        mPlayerEventListener.onError(error)
    }

    override fun onCues(cues: MutableList<Cue>) {
        super.onCues(cues)
        if (subtitleType == SubtitleType.UN_KNOW && cues.size == 0)
            return

        //字幕类型仅在第一次输出时初始化
        if (subtitleType == SubtitleType.UN_KNOW) {
            subtitleType = if (cues[0].bitmap != null)
                SubtitleType.BITMAP
            else
                SubtitleType.TEXT
        }

        if (subtitleType == SubtitleType.BITMAP) {
            //以图片输出字幕
            mPlayerEventListener.onSubtitleTextOutput(
                MixedSubtitle(
                    SubtitleType.BITMAP,
                    null,
                    cues
                )
            )
        } else {
            //以文字输出字幕
            val textBuilder = StringBuilder()
            for (cue in cues) {
                textBuilder.append(cue.text).append("\n")
            }
            val subtitleText = if (textBuilder.isNotEmpty())
                textBuilder.substring(0, textBuilder.length - 1)
            else
                textBuilder.toString()
            mPlayerEventListener.onSubtitleTextOutput(
                MixedSubtitle(SubtitleType.TEXT, subtitleText)
            )
        }
    }

    private fun initListener() {
        mMediaSourceEventListener = object : MediaSourceEventListener {
            override fun onLoadStarted(
                windowIndex: Int,
                mediaPeriodId: MediaSource.MediaPeriodId?,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadStarted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData)
                mPlayerEventListener.onPrepared()
            }
        }
        exoplayer.addListener(this)
    }

    private fun getStreams(isAudio: Boolean): List<VideoStreamBean> {
        val targetType = if (isAudio)
            C.TRACK_TYPE_AUDIO
        else
            C.TRACK_TYPE_TEXT

        val streams = mutableListOf<VideoStreamBean>()
        val trackNameProvider = DefaultTrackNameProvider(mContext.resources)

        for (trackGroupIndex in 0 until exoplayer.currentTracks.groups.size) {
            val trackGroup = exoplayer.currentTracks.groups[trackGroupIndex]
            if (trackGroup.type != targetType) {
                continue
            }

            for (index in 0 until trackGroup.length) {
                val isSelected = trackGroup.isTrackSelected(index)
                val trackFormat = trackGroup.getTrackFormat(index)
                val stream = VideoStreamBean(
                    trackName = trackNameProvider.getTrackName(trackFormat),
                    isAudio = isAudio,
                    trackId = index,
                    isChecked = isSelected,
                    trackGroupId = trackGroupIndex
                )
                streams.add(stream)
            }
        }
        return streams
    }

    fun setTrackSelector(trackSelector: TrackSelector) {
        mTrackSelector = trackSelector
    }

    fun setRenderersFactory(renderersFactory: RenderersFactory) {
        mRenderersFactory = renderersFactory
    }

    fun setLoadControl(loadControl: LoadControl) {
        mLoadControl = loadControl
    }
}