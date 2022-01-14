package com.xyoye.player.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.LoadDanmuState
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.controller.base.GestureVideoController
import com.xyoye.player.controller.danmu.DanmuController
import com.xyoye.player.controller.setting.SettingController
import com.xyoye.player.controller.subtitle.SubtitleController
import com.xyoye.player.controller.video.LoadingView
import com.xyoye.player.controller.video.PlayerBottomView
import com.xyoye.player.controller.video.PlayerGestureView
import com.xyoye.player.controller.video.PlayerTopView
import com.xyoye.player.utils.MessageTime
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutPlayerControllerBinding
import com.xyoye.player_component.utils.BatteryHelper
import com.xyoye.subtitle.MixedSubtitle

/**
 * Created by xyoye on 2020/11/3.
 */

class VideoController(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GestureVideoController(context, attrs, defStyleAttr) {
    //弹幕视图控制器
    private val mDanmuController = DanmuController(context)

    //字幕视图控制器
    private val mSubtitleController = SubtitleController(context)

    //设置视图控制器
    private val mSettingController = SettingController(context)

    private val playerTopView = PlayerTopView(context)
    private val playerBotView = PlayerBottomView(context)
    private val gestureView = PlayerGestureView(context)
    private val loadingView = LoadingView(context)

    private var lastPlayPosition = 0L

    private var mDanmuSourceChanged: ((String, Int) -> Unit)? = null
    private var mSubtitleSourceChanged: ((String) -> Unit)? = null

    private val controllerBinding = DataBindingUtil.inflate<LayoutPlayerControllerBinding>(
        LayoutInflater.from(context),
        R.layout.layout_player_controller,
        this,
        true
    )

    init {
        addControlComponent(mDanmuController.getView())
        addControlComponent(*mSubtitleController.getViews())
        addControlComponent(gestureView)
        addControlComponent(playerTopView)
        addControlComponent(playerBotView)
        addControlComponent(loadingView)
        addControlComponent(*mSettingController.getViews())

        controllerBinding.playerLockIv.setOnClickListener {
            mControlWrapper.toggleLockState()
        }

        controllerBinding.playerShotIv.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.SCREEN_SHOT)
        }
    }

    override fun getDanmuController() = mDanmuController

    override fun getSubtitleController() = mSubtitleController

    override fun getSettingController() = mSettingController

    override fun showMessage(text: String, time: MessageTime) {
        controllerBinding.messageContainer.showMessage(text, time)
    }

    override fun onDanmuSourceUpdate(danmuPath: String, episodeId: Int) {
        val videoSource = mControlWrapper.getVideoSource()
        if (videoSource.getDanmuPath() == danmuPath
            && videoSource.getEpisodeId() == episodeId
        ) {
            return
        }
        mDanmuSourceChanged?.invoke(danmuPath, episodeId)
    }

    override fun onSubtitleSourceUpdate(subtitlePath: String) {
        val videoSource = mControlWrapper.getVideoSource()
        if (videoSource.getSubtitlePath() == subtitlePath) {
            return
        }
        mSubtitleSourceChanged?.invoke(subtitlePath)
    }

    override fun onLockStateChanged(isLocked: Boolean) {
        controllerBinding.playerLockIv.isSelected = isLocked
        updateShotVisible(!isLocked)
    }

    override fun onVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            if (isLocked()) {
                controllerBinding.playerLockIv.postDelayed({
                    controllerBinding.playerLockIv.requestFocus()
                }, 100)
            }
            controllerBinding.playerLockIv.isVisible = true
            ViewCompat.animate(controllerBinding.playerLockIv).translationX(0f).setDuration(300)
                .start()
        } else {
            playerTopView.findViewById<TextView>(R.id.video_title_tv).requestFocus()
            val translateX = dp2px(60).toFloat()
            ViewCompat.animate(controllerBinding.playerLockIv).translationX(-translateX)
                .setDuration(300).start()
        }

        if (isLocked()) {
            return
        }
        updateShotVisible(isVisible)
    }

    override fun onPlayStateChanged(playState: PlayState) {
        super.onPlayStateChanged(playState)
        if (playState == PlayState.STATE_PLAYING) {
            considerSeekToLastPlay()
        }
    }

    override fun onBackPressed(): Boolean {
        if (isLocked()) {
            showController()
            return true
        }
        if (isControllerShowing()) {
            mControlWrapper.hideController()
            return true
        }
        if (mControlWrapper.isSettingViewShowing()) {
            mControlWrapper.hideSettingView()
            return true
        }
        return super.onBackPressed()
    }

    override fun release() {
        super.release()
        lastPlayPosition = 0
        controllerBinding.messageContainer.clearMessage()
    }

    override fun destroy() {

    }

    /**
     * 设置视频标题
     */
    fun setVideoTitle(title: String?) {
        playerTopView.setVideoTitle(title)
    }

    /**
     * 设置初始弹幕路径
     */
    fun setDanmuPath(url: String?) {
        mControlWrapper.onDanmuSourceChanged(url ?: "")
    }

    /**
     * 设置初始字幕路径
     */
    fun setSubtitlePath(url: String?) {
        if (url.isNullOrEmpty())
            return
        mControlWrapper.setSubtitlePath(url)
    }

    /**
     * 设置上次播放位置
     */
    fun setLastPosition(position: Long) {
        lastPlayPosition = position
    }

    /**
     * 设置电量数据
     */
    fun setBatteryHelper(helper: BatteryHelper) {
        playerTopView.setBatteryHelper(helper)
    }

    /**
     * 播放错误回调
     */
    fun observerPlayError(block: () -> Unit) {
        mPlayErrorBlock = block
    }

    /**
     * 退出播放回调
     */
    fun observerPlayExit(block: () -> Unit) {
        playerTopView.setExitObserver(block)
    }

    /**
     * 弹幕资源更新回调
     */
    fun observeDanmuSourceChanged(block: (danmuPath: String, episodeId: Int) -> Unit) {
        mDanmuSourceChanged = block
    }

    /**
     * 字幕资源更新回调
     */
    fun observeSubtitleSourceChanged(block: (subtitle: String) -> Unit) {
        mSubtitleSourceChanged = block
    }

    /**
     * 发送弹幕回调
     */
    fun observerSendDanmu(block: (danmuData: SendDanmuBean) -> Unit) {
        playerBotView.setSendDanmuBlock(block)
    }

    /**
     * 切换视频资源回调
     */
    fun setSwitchVideoSourceBlock(block: (Int) -> Unit) {
        playerBotView.setSwitchVideoSourceBlock(block)
        mSettingController.setSwitchVideoSourceBlock(block)
    }

    /**
     * 弹幕屏蔽回调
     */
    fun observerDanmuBlock(
        cloudBlock: LiveData<MutableList<DanmuBlockEntity>>? = null,
        add: ((keyword: String, isRegex: Boolean) -> Unit),
        remove: ((id: Int) -> Unit),
        queryAll: () -> LiveData<MutableList<DanmuBlockEntity>>
    ) {
        mDanmuController.setCloudBlockLiveData(cloudBlock)
        mSettingController.setDatabaseBlock(add, remove, queryAll)
    }

    /**
     * 更新字幕内容
     *
     * 由播放器调用
     */
    fun updateSubtitle(subtitle: MixedSubtitle) {
        mSubtitleController.updateSubtitle(subtitle)
    }

    /**
     * 更新音频/字幕流
     */
    fun updateTrack(isAudio: Boolean, trackData: MutableList<VideoTrackBean>) {
        mSettingController.updateTrack(isAudio, trackData)
    }

    /**
     * 更新自动匹配弹幕状态
     */
    fun updateLoadDanmuState(state: LoadDanmuState) {
        mSettingController.updateLoadDanmuState(state)
    }

    private fun updateShotVisible(isVisible: Boolean) {
        if (isVisible) {
            controllerBinding.playerShotIv.isVisible = true
            ViewCompat.animate(controllerBinding.playerShotIv).translationX(0f).setDuration(300)
                .start()
        } else {
            val translateX = dp2px(60).toFloat()
            ViewCompat.animate(controllerBinding.playerShotIv).translationX(translateX)
                .setDuration(300).start()
        }
    }

    private fun considerSeekToLastPlay() {
        if (lastPlayPosition <= 0)
            return
        mControlWrapper.seekTo(lastPlayPosition)
        showMessage("已为你定位至：${formatDuration(lastPlayPosition)}", MessageTime.LONG)
        lastPlayPosition = 0
    }
}