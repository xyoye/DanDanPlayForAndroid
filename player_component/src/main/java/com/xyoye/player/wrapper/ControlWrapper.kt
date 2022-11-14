package com.xyoye.player.wrapper

import android.graphics.PointF
import android.view.KeyEvent
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.bean.VideoStreamBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.utils.MessageTime

/**
 * Created by xyoye on 2020/11/1.
 *
 * 控制器包装类
 *
 * 用于实现不同控制器间的交互
 */

class ControlWrapper(
    private val mVideoPlayer: InterVideoPlayer,
    private val mController: InterVideoController,
    private val mDanmuController: InterDanmuController,
    private val mSubtitleController: InterSubtitleController,
    private val mSettingController: InterSettingController
) : InterVideoPlayer, InterVideoController, InterDanmuController, InterSubtitleController,
    InterSettingController {

    /**
     * ------------------Player Controller----------------------
     */

    override fun start() {
        mVideoPlayer.start()
    }

    override fun pause() {
        mVideoPlayer.pause()
    }

    override fun getVideoSource() = mVideoPlayer.getVideoSource()

    override fun getDuration() = mVideoPlayer.getDuration()

    override fun getCurrentPosition() = mVideoPlayer.getCurrentPosition()

    override fun seekTo(timeMs: Long) {
        //播放器
        mVideoPlayer.seekTo(timeMs)
        //弹幕
        seekTo(timeMs, isPlaying())
        //视图
        if (isPlaying()) {
            startProgress()
        } else {
            setProgress(timeMs)
        }
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
        mDanmuController.setSpeed(speed)
    }

    override fun getSpeed() = mVideoPlayer.getSpeed()

    override fun getTcpSpeed() = mVideoPlayer.getTcpSpeed()

    override fun doScreenShot() = mVideoPlayer.doScreenShot()

    override fun getVideoSize() = mVideoPlayer.getVideoSize()

    override fun setRotation(rotation: Float) {
        mVideoPlayer.setRotation(rotation)
    }

    override fun interceptSubtitle(subtitlePath: String) =
        mVideoPlayer.interceptSubtitle(subtitlePath)

    override fun getAudioStream(): List<VideoStreamBean> {
        return mVideoPlayer.getAudioStream()
    }

    override fun getSubtitleStream(): List<VideoStreamBean> {
        return mVideoPlayer.getSubtitleStream()
    }

    override fun selectStream(stream: VideoStreamBean) {
        return mVideoPlayer.selectStream(stream)
    }

    /**
     * ------------------Video Controller----------------------
     */

    override fun startFadeOut() {
        mController.startFadeOut()
    }

    override fun stopFadeOut() {
        mController.stopFadeOut()
    }

    override fun isControllerShowing() = mController.isControllerShowing()

    override fun showMessage(text: String, time: MessageTime) {
        mController.showMessage(text, time)
    }

    override fun setLocked(locked: Boolean) {
        mController.setLocked(locked)
    }

    override fun isLocked() = mController.isLocked()

    override fun setPopupMode(isPopup: Boolean) {
        mController.setPopupMode(isPopup)
    }

    override fun isPopupMode() = mController.isPopupMode()

    override fun startProgress() {
        mController.startProgress()
    }

    override fun stopProgress() {
        mController.stopProgress()
    }

    override fun setProgress(position: Long) {
        mController.setProgress(position)
    }

    override fun hideController() {
        mController.hideController()
    }

    override fun showController(ignoreShowing: Boolean) {
        mController.showController(ignoreShowing)
    }

    override fun onDanmuSourceUpdate(danmuPath: String, episodeId: Int) {
        mController.onDanmuSourceUpdate(danmuPath, episodeId)
    }

    override fun onSubtitleSourceUpdate(subtitlePath: String) {
        mController.onSubtitleSourceUpdate(subtitlePath)
    }

    override fun destroy() {
        mController.destroy()
    }

    /**
     * ------------------Danmu Controller----------------------
     */

    override fun getDanmuUrl(): String? {
        return mDanmuController.getDanmuUrl()
    }

    override fun updateDanmuSize() {
        mDanmuController.updateDanmuSize()
    }

    override fun updateDanmuSpeed() {
        mDanmuController.updateDanmuSpeed()
    }

    override fun updateDanmuAlpha() {
        mDanmuController.updateDanmuAlpha()
    }

    override fun updateDanmuStoke() {
        mDanmuController.updateDanmuStoke()
    }

    override fun updateDanmuOffsetTime() {
        mDanmuController.updateDanmuOffsetTime()
    }

    override fun danmuRelease() {
        mDanmuController.danmuRelease()
    }

    override fun updateMobileDanmuState() {
        mDanmuController.updateMobileDanmuState()
    }

    override fun updateTopDanmuState() {
        mDanmuController.updateTopDanmuState()
    }

    override fun updateBottomDanmuState() {
        mDanmuController.updateBottomDanmuState()
    }

    override fun updateMaxLine() {
        mDanmuController.updateMaxLine()
    }

    override fun updateMaxScreenNum() {
        mDanmuController.updateMaxScreenNum()
    }

    override fun toggleDanmuVisible() {
        mDanmuController.toggleDanmuVisible()
    }

    override fun onDanmuSourceChanged(filePath: String, episodeId: Int) {
        mDanmuController.onDanmuSourceChanged(filePath)
        mSettingController.onDanmuSourceChanged()
        mController.onDanmuSourceUpdate(filePath, episodeId)
    }

    override fun allowSendDanmu(): Boolean {
        return mDanmuController.allowSendDanmu()
    }

    override fun addDanmuToView(danmuBean: SendDanmuBean) {
        mDanmuController.addDanmuToView(danmuBean)
    }

    override fun addBlackList(isRegex: Boolean, vararg keyword: String) {
        mDanmuController.addBlackList(isRegex, *keyword)
    }

    override fun removeBlackList(isRegex: Boolean, keyword: String) {
        mDanmuController.removeBlackList(isRegex, keyword)
    }

    override fun seekTo(timeMs: Long, isPlaying: Boolean) {
        mDanmuController.seekTo(timeMs, isPlaying)
    }

    /**
     * ------------------Subtitle Controller----------------------
     */

    override fun setSubtitleLoadedCallback(callback: ((String, Boolean) -> Unit)?) {
        mSubtitleController.setSubtitleLoadedCallback(callback)
    }

    override fun setImageSubtitleEnable(enable: Boolean) {
        mSubtitleController.setImageSubtitleEnable(enable)
    }

    override fun setTextSubtitleDisable() {
        mSubtitleController.setTextSubtitleDisable()
    }

    override fun showExternalTextSubtitle() {
        mSubtitleController.showExternalTextSubtitle()
    }

    override fun showInnerTextSubtitle() {
        mSubtitleController.showInnerTextSubtitle()
    }

    override fun setSubtitlePath(subtitlePath: String, playWhenReady: Boolean) {
        //是否由播放器处理外挂字幕
        if (interceptSubtitle(subtitlePath))
            return
        //由字幕控件处理外挂字幕
        mSubtitleController.setSubtitlePath(subtitlePath, playWhenReady)
    }

    override fun updateTextSize() {
        mSubtitleController.updateTextSize()
    }

    override fun updateStrokeWidth() {
        mSubtitleController.updateStrokeWidth()
    }

    override fun updateTextColor() {
        mSubtitleController.updateTextColor()
    }

    override fun updateStrokeColor() {
        mSubtitleController.updateStrokeColor()
    }

    override fun updateSubtitleOffsetTime() {
        mSubtitleController.updateSubtitleOffsetTime()
        mVideoPlayer.updateSubtitleOffsetTime()
    }

    override fun subtitleRelease() {
        mSubtitleController.subtitleRelease()
    }

    /**
     * ------------------Setting Controller----------------------
     */
    override fun isSettingViewShowing() = mSettingController.isSettingViewShowing()

    override fun hideSettingView() {
        mSettingController.hideSettingView()
    }

    override fun onDanmuSourceChanged() {
        mSettingController.onDanmuSourceChanged()
    }

    override fun onSubtitleSourceChanged() {
        mSettingController.onSubtitleSourceChanged()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mSettingController.onKeyDown(keyCode, event)
    }

    override fun showSettingView(viewType: SettingViewType) {
        hideController()
        if (!isLocked()) {
            mSettingController.showSettingView(viewType)
        }
    }

    override fun settingRelease() {
        mSettingController.settingRelease()
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
        if (isSettingViewShowing()) {
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