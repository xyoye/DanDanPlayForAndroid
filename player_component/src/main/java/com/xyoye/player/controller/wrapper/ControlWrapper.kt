package com.xyoye.player.controller.wrapper

import android.graphics.PointF
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.controller.interfaces.InterVideoController
import com.xyoye.player.controller.interfaces.InterVideoPlayer

/**
 * Created by xyoye on 2020/11/1.
 */

class ControlWrapper(
    private val mVideoPlayer: InterVideoPlayer,
    private val mController: InterVideoController
) : InterVideoPlayer, InterVideoController {

    override fun start() {
        mVideoPlayer.start()
    }

    override fun pause() {
        mVideoPlayer.pause()
    }

    override fun getDuration() = mVideoPlayer.getDuration()

    override fun getCurrentPosition() = mVideoPlayer.getCurrentPosition()

    override fun seekTo(timeMs: Long) {
        mVideoPlayer.seekTo(timeMs)
        mController.seekTo(timeMs)
    }

    override fun isPlaying() = mVideoPlayer.isPlaying()

    override fun getBufferedPercentage() = mVideoPlayer.getBufferedPercentage()

    override fun setSilence(isSilence: Boolean) {
        mVideoPlayer.setSilence(isSilence)
    }

    override fun isSilence() = mVideoPlayer.isSilence()

    override fun setVolume(point: PointF) {
        mVideoPlayer.setVolume(point)
    }

    override fun getVolume() = mVideoPlayer.getVolume()

    override fun setScreenScale(scaleType: VideoScreenScale) {
        mVideoPlayer.setScreenScale(scaleType)
    }

    override fun setSpeed(speed: Float) {
        mVideoPlayer.setSpeed(speed)
    }

    override fun getSpeed() = mVideoPlayer.getSpeed()

    override fun getTcpSpeed() = mVideoPlayer.getTcpSpeed()

    override fun setMirrorRotation(enable: Boolean) {
        mVideoPlayer.setMirrorRotation(enable)
    }

    override fun doScreenShot() = mVideoPlayer.doScreenShot()

    override fun getVideoSize() = mVideoPlayer.getVideoSize()

    override fun setRotation(rotation: Float) {
        mVideoPlayer.setRotation(rotation)
    }

    override fun selectTrack(select: VideoTrackBean?, deselect: VideoTrackBean?) {
        mVideoPlayer.selectTrack(select, deselect)
    }

    /**
     * 切换播放状态
     */
    fun togglePlay() {
        if (isPlaying())
            pause()
        else
            start()
    }

    override fun startFadeOut() {
        mController.startFadeOut()
    }

    override fun stopFadeOut() {
        mController.stopFadeOut()
    }

    override fun isControllerShowing() = mController.isControllerShowing()

    override fun isSettingViewShowing() = mController.isSettingViewShowing()

    override fun setLocked(locked: Boolean) {
        mController.setLocked(locked)
    }

    override fun isLocked() = mController.isLocked()

    override fun startProgress() {
        mController.startProgress()
    }

    override fun stopProgress() {
        mController.stopProgress()
    }

    override fun hideSettingView() {
        mController.hideSettingView()
    }

    override fun hideController() {
        mController.hideController()
    }

    override fun showController() {
        mController.showController()
    }

    override fun destroy() {
        mController.destroy()
    }

    override fun showSettingView(viewType: SettingViewType) {
        mController.showSettingView(viewType)
    }

    override fun toggleDanmuVisible() {
        mController.toggleDanmuVisible()
    }

    override fun switchSubtitleSource() {
        mController.switchSubtitleSource()
    }

    override fun switchDanmuSource() {
        mController.switchDanmuSource()
    }

    override fun allowSendDanmu(): Boolean {
        return mController.allowSendDanmu()
    }

    override fun addDanmuToView(danmuBean: SendDanmuBean) {
        mController.addDanmuToView(danmuBean)
    }

    /**
     * 切换视图锁定状态
     */
    fun toggleLockState() {
        startFadeOut()
        setLocked(!isLocked())
    }

    /**
     * 切换视图显示状态
     */
    fun toggleVisible() {
        if (isSettingViewShowing()){
            hideSettingView()
            return
        }
        if (isControllerShowing()) {
            hideController()
        } else {
            showController()
        }
    }
}