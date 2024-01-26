package com.xyoye.player.wrapper

import android.graphics.PointF
import android.view.KeyEvent
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.DanmakuLanguage
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.TrackType
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.utils.MessageTime
import com.xyoye.subtitle.MixedSubtitle

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
) : InterVideoPlayer,
    InterVideoController,
    InterDanmuController,
    InterSubtitleController,
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

    override fun getRenderView() = mVideoPlayer.getRenderView()

    override fun getVideoSize() = mVideoPlayer.getVideoSize()

    override fun setRotation(rotation: Float) {
        mVideoPlayer.setRotation(rotation)
    }

    /**
     * ------------------Player Track Controller----------------------
     */

    override fun supportAddTrack(type: TrackType): Boolean {
        return mVideoPlayer.supportAddTrack(type)
    }

    override fun addTrack(track: VideoTrackBean): Boolean {
        // 如果视频播放器支持添加轨道，则直接添加
        // 否则由支持轨道的控制器添加
        val trackType = track.type
        val added = if (mVideoPlayer.supportAddTrack(trackType)) {
            mVideoPlayer.addTrack(track)
        } else if (mSubtitleController.supportAddTrack(trackType)) {
            mSubtitleController.addTrack(track)
        } else if (mDanmuController.supportAddTrack(trackType)) {
            mDanmuController.addTrack(track)
        } else {
            false
        }

        if (added) {
            // 添加轨道成功，设置轨道选中
            selectTrack(track)

            mController.setTrackAdded(track)
        }
        return added
    }

    override fun getTracks(type: TrackType): List<VideoTrackBean> {
        // 如果视频播放器支持添加轨道，则直接获取播放器的轨道
        if (mVideoPlayer.supportAddTrack(type)) {
            return mVideoPlayer.getTracks(type)
        }

        // 获取播放器的轨道和控制器的轨道
        val tracks = mVideoPlayer.getTracks(type).toMutableList()
        if (type == TrackType.SUBTITLE) {
            tracks.addAll(mSubtitleController.getTracks(type))
        } else if (type == TrackType.DANMU) {
            tracks.addAll(mDanmuController.getTracks(type))
        }
        return tracks
    }

    override fun selectTrack(track: VideoTrackBean) {
        // 如果视频播放器支持添加轨道，则选中播放器轨道，并取消控制器中同类型轨道的选中
        // 否则由支持轨道的控制器选中，并取消播放器中同类型轨道的选中
        val trackType = track.type
        if (mVideoPlayer.supportAddTrack(trackType)) {
            mVideoPlayer.selectTrack(track)
            mSubtitleController.deselectTrack(trackType)
            mDanmuController.deselectTrack(trackType)
        } else if (mSubtitleController.supportAddTrack(trackType)) {
            mVideoPlayer.deselectTrack(trackType)
            mSubtitleController.selectTrack(track)
        } else if (mDanmuController.supportAddTrack(trackType)) {
            mVideoPlayer.deselectTrack(trackType)
            mDanmuController.selectTrack(track)
        }
        mController.setTrackUpdated(trackType)
    }

    override fun deselectTrack(type: TrackType) {
        mVideoPlayer.deselectTrack(type)
        mSubtitleController.deselectTrack(type)
        mDanmuController.deselectTrack(type)
        mController.setTrackUpdated(type)
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

    override fun setTrackAdded(track: VideoTrackBean) {
        mController.setTrackAdded(track)
    }

    override fun setTrackUpdated(type: TrackType) {
        mController.setTrackUpdated(type)
    }

    override fun destroy() {
        mController.destroy()
    }

    /**
     * ------------------Danmu Controller----------------------
     */

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

    override fun setLanguage(language: DanmakuLanguage) {
        mDanmuController.setLanguage(language)
        mDanmuController.seekTo(getCurrentPosition(), isPlaying())
    }

    /**
     * ------------------Subtitle Controller----------------------
     */

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

    override fun onSubtitleTextOutput(subtitle: MixedSubtitle) {
        mSubtitleController.onSubtitleTextOutput(subtitle)
    }

    /**
     * ------------------Setting Controller----------------------
     */
    override fun isSettingViewShowing() = mSettingController.isSettingViewShowing()

    override fun hideSettingView() {
        mSettingController.hideSettingView()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mSettingController.onKeyDown(keyCode, event)
    }

    override fun showSettingView(viewType: SettingViewType, extra: Any?) {
        hideController()
        if (!isLocked()) {
            mSettingController.showSettingView(viewType, extra)
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