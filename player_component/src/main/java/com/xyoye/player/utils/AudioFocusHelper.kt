package com.xyoye.player.utils

import android.graphics.PointF
import android.media.AudioManager
import androidx.lifecycle.LifecycleCoroutineScope
import com.xyoye.player.controller.interfaces.InterVideoPlayer
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Created by xyoye on 2020/11/3.
 */

class AudioFocusHelper(
    player: InterVideoPlayer,
    private val mAudioManager: AudioManager,
    private val coroutineScope: LifecycleCoroutineScope
) :
    AudioManager.OnAudioFocusChangeListener {

    var enable = false
    private var mStartRequested = false
    private var mPausedForLoss = false
    private var mCurrentFocus = 0

    private val mPlayerWeak = WeakReference<InterVideoPlayer>(player)

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
        val status: Int = mAudioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        )
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
        mAudioManager.abandonAudioFocus(this)
    }
}