package com.xyoye.player.controller

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.LiveData
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.controller.base.GestureVideoController
import com.xyoye.player.controller.danmu.DanmuController
import com.xyoye.player.controller.setting.SettingController
import com.xyoye.player.controller.subtitle.SubtitleController
import com.xyoye.player.controller.video.LoadingView
import com.xyoye.player.controller.video.PlayerBottomView
import com.xyoye.player.controller.video.PlayerControlView
import com.xyoye.player.controller.video.PlayerGestureView
import com.xyoye.player.controller.video.PlayerPopupControlView
import com.xyoye.player.controller.video.PlayerTopView
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.utils.MessageTime
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
    private val mSettingController = SettingController(context) {
        addControlComponent(it)
    }

    private val playerTopView = PlayerTopView(context)
    private val playerBotView = PlayerBottomView(context)
    private val gestureView = PlayerGestureView(context)
    private val loadingView = LoadingView(context)

    private val playerControlView = PlayerControlView(context)
    private val playerPopupControlView = PlayerPopupControlView(context)

    private var lastPlayPosition = 0L
    private var lastVideoSpeed: Float? = null

    private var mDanmuSourceChanged: ((String, String?) -> Unit)? = null
    private var mSubtitleSourceChanged: ((String) -> Unit)? = null
    private var switchVideoSourceBlock: ((Int) -> Unit)? = null

    init {
        addControlComponent(mDanmuController.getView())
        addControlComponent(*mSubtitleController.getViews())
        addControlComponent(gestureView)
        addControlComponent(playerTopView)
        addControlComponent(playerBotView)
        addControlComponent(loadingView)
        addControlComponent(playerControlView)
    }

    override fun getDanmuController() = mDanmuController

    override fun getSubtitleController() = mSubtitleController

    override fun getSettingController() = mSettingController

    override fun showMessage(text: String, time: MessageTime) {
        playerControlView.showMessage(text, time)
    }

    override fun onDanmuSourceUpdate(danmuPath: String, episodeId: String?) {
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

    override fun onPopupModeChanged(isPopup: Boolean) {
        super.onPopupModeChanged(isPopup)
        mSettingController.setPopupMode(isPopup)

        if (isPopup) {
            addControlComponent(playerPopupControlView)

            removeControlComponent(gestureView)
            removeControlComponent(playerTopView)
            removeControlComponent(playerBotView)
            removeControlComponent(playerControlView)
        } else {
            removeControlComponent(playerPopupControlView)

            addControlComponent(gestureView)
            addControlComponent(playerTopView)
            addControlComponent(playerBotView)
            addControlComponent(playerControlView)
        }
    }

    override fun onPlayStateChanged(playState: PlayState) {
        super.onPlayStateChanged(playState)
        if (playState == PlayState.STATE_PLAYING) {
            considerSeekToLastPlay()
            considerSetVideoSpeed()
        } else if (playState == PlayState.STATE_COMPLETED) {
            if (PlayerInitializer.Player.isAutoPlayNext) {
                val videoSource = mControlWrapper.getVideoSource()
                if (videoSource.hasNextSource()) {
                    switchVideoSourceBlock?.invoke(videoSource.getGroupIndex() + 1)
                    return
                }
            }
            mPlayCompletionBlock?.invoke()
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
        playerControlView.clearMessage()
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
        mControlWrapper.addSubtitleStream(url)
    }

    /**
     * 设置上次播放位置
     */
    fun setLastPosition(position: Long) {
        lastPlayPosition = position
    }

    /**
     * 设置上次播放速度
     */
    fun setLastPlaySpeed(speed: Float) {
        lastVideoSpeed = speed
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
    fun observerExitPlayer(block: () -> Unit) {
        mPlayCompletionBlock = block
        playerTopView.setExitPlayerObserver(block)
        playerPopupControlView.setExitPlayerObserver(block)
    }

    /**
     * 进入悬浮窗模式回调
     */
    fun observerEnterPopupMode(block: () -> Unit) {
        playerTopView.setEnterPopupModeObserver(block)
    }

    /**
     * 退出悬浮窗模式回调
     */
    fun observerExitPopupMode(block: () -> Unit) {
        playerPopupControlView.setExitPopupModeObserver(block)
    }

    /**
     * 弹幕资源更新回调
     */
    fun observeDanmuSourceChanged(block: (danmuPath: String, episodeId: String?) -> Unit) {
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
        this.switchVideoSourceBlock = block
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
     * 弹幕搜索
     */
    fun observerDanmuSearch(
        search: (String) -> Unit,
        download: (DanmuEpisodeData) -> Unit,
        searchResult: () -> LiveData<List<DanmuEpisodeData>>
    ) {
        mSettingController.setDanmuSearch(search, download, searchResult)
    }

    /**
     * 更新字幕内容
     *
     * 由播放器调用
     */
    fun onSubtitleTextOutput(subtitle: MixedSubtitle) {
        mSubtitleController.onSubtitleTextOutput(subtitle)
    }

    private fun considerSeekToLastPlay() {
        if (lastPlayPosition <= 0)
            return

        //上次进度大于90%时，不执行自动定位进度
        val duration = mControlWrapper.getDuration()
        if (1.0 * lastPlayPosition / duration >= 0.9) {
            return
        }

        mControlWrapper.seekTo(lastPlayPosition)
        showMessage("已为你定位至：${formatDuration(lastPlayPosition)}", MessageTime.LONG)
        lastPlayPosition = 0
    }

    private fun considerSetVideoSpeed() {
        lastVideoSpeed?.let {
            mControlWrapper.setSpeed(it)
        }
        lastVideoSpeed = null
    }
}