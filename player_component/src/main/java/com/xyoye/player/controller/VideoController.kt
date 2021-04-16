package com.xyoye.player.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.player.controller.impl.GestureVideoController
import com.xyoye.player.controller.setting.SettingController
import com.xyoye.player.controller.subtitle.SubtitleController
import com.xyoye.player.controller.video.*
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutPlayerControllerBinding
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
    private val skipPositionView = SkipPositionView(context)
    private val loadingView = LoadingView(context)

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
        addControlComponent(skipPositionView)
        addControlComponent(loadingView)
        addControlComponent(*mSettingController.getViews())

        controllerBinding.playerLockIv.setOnClickListener {
            mControlWrapper.toggleLockState()
        }

        controllerBinding.playerShotIv.setOnClickListener {
            val shotBitmap = mControlWrapper.doScreenShot()
            if (shotBitmap == null) {
                ToastCenter.showOriginalToast("当前渲染器不支持截屏")
                return@setOnClickListener
            }
            mControlWrapper.pause()
            mControlWrapper.hideController()
            ScreenShotDialog(context, shotBitmap).show()
        }
    }

    override fun getDanmuController() = mDanmuController

    override fun getSubtitleController() = mSubtitleController

    override fun getSettingController() = mSettingController

    override fun onLockStateChanged(isLocked: Boolean) {
        controllerBinding.playerLockIv.isSelected = isLocked
        updateShotVisible(!isLocked)
    }

    override fun onVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            controllerBinding.playerLockIv.isVisible = true
            ViewCompat.animate(controllerBinding.playerLockIv).translationX(0f).setDuration(300)
                .start()
        } else {
            val translateX = dp2px(60).toFloat()
            ViewCompat.animate(controllerBinding.playerLockIv).translationX(-translateX)
                .setDuration(300).start()
        }

        if (isLocked()) {
            return
        }
        updateShotVisible(isVisible)
    }

    override fun onBackPressed(): Boolean {
        if (isLocked()) {
            showController()
            return true
        }
        return super.onBackPressed()
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
        mDanmuController.setDanmuPath(url)
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
        skipPositionView.setSkipPosition(position)
    }

    /**
     * 设置电量
     */
    fun setBatteryChanged(percent: Int) {
        playerTopView.setBatteryChange(percent)
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
     * 资源绑定回调
     */
    fun observerBindSource(block: (sourcePath: String, isSubtitle: Boolean) -> Unit) {
        mSettingController.setBindSourceObserver(block)
    }

    /**
     * 发送弹幕回调
     */
    fun observerSendDanmu(block: (danmuData: SendDanmuBean) -> Unit) {
        playerBotView.setSendDanmuBlock(block)
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

    fun updateTrack(isAudio: Boolean, trackData: MutableList<VideoTrackBean>) {
        mSettingController.updateTrack(isAudio, trackData)
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
}