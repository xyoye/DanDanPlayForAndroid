package com.xyoye.player.utils

import android.graphics.PointF
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.lifecycle.LifecycleCoroutineScope
import com.xyoye.player.wrapper.InterVideoPlayer
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Created by xyoye on 2020/11/3.
 */

class AudioFocusHelper(
    player: InterVideoPlayer,
    private val mAudioManager: AudioManager,
    private val coroutineScope: LifecycleCoroutineScope
) : AudioManager.OnAudioFocusChangeListener {

    var enable = false
    private var mStartRequested = false
    private var mPausedForLoss = false
    private var mCurrentFocus = 0

    private val mPlayerWeak = WeakReference(player)
    private var audioFocusRequest : AudioFocusRequest? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes
                .Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
            audioFocusRequest = AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener(this)
                .build()
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (mCurrentFocus == focusChange || !enable)
            return
        mCurrentFocus = focusChange

        val mPlayer = mPlayerWeak.get() ?: return
        coroutineScope.launch {
            when (mCurrentFocus) {
                AudioManager.AUDIOFOCUS_GAIN,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                    if (mStartRequested || mPausedForLoss) {
                        mPlayer.start()
                        mStartRequested = false
                        mPausedForLoss = false
                    }
                    if (!mPlayer.isSilence()) {
                        mPlayer.setVolume(PointF(1f, 1f))
                    }
                }
                //失去焦点暂停播放
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (mPlayer.isPlaying()) {
                        mPausedForLoss = true
                        mPlayer.pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    if (mPlayer.isPlaying() && !mPlayer.isSilence()) {
                        mPlayer.setVolume(PointF(0.1f, 0.1f))
                    }
                }
            }
        }
    }

    fun requestFocus() {
        if (!enable)
            return
        if (mCurrentFocus == AudioManager.AUDIOFOCUS_GAIN) {
            //如果已经是获得焦点，则直接返回
            return
        }
        //请求重新获取焦点

        val request = audioFocusRequest
        val status: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && request != null) {
            mAudioManager.requestAudioFocus(request)
        } else {
            mAudioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC
                , AudioManager.AUDIOFOCUS_GAIN
            )
        }

        //焦点更改请求成功
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
            mCurrentFocus = AudioManager.AUDIOFOCUS_GAIN
            return
        }
        mStartRequested = true
    }

    fun abandonFocus() {
        if (!enable)
            return
        mStartRequested = false

        val request = audioFocusRequest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && request != null) {
            mAudioManager.abandonAudioFocusRequest(request)
        } else {
            mAudioManager.abandonAudioFocus(this)
        }
    }
}