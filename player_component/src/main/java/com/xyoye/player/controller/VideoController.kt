package com.xyoye.player.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.controller.impl.GestureVideoController
import com.xyoye.player.controller.interfaces.BatteryObserver
import com.xyoye.player.controller.view.*
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

    private val danmuView = DanmuView(context)
    private val subtitleTextView = SubtitleTextView(context)
    private val subtitleImageView = SubtitleImageView(context)
    private val playerTopView = PlayerTopView(context)
    private val playerBotView = PlayerBottomView(context)
    private val gestureView = PlayerGestureView(context)

    private val playerSettingView = SettingPlayerView(context)
    private val danmuSettingView = SettingDanmuView(context, danmuView)
    private val subtitleSettingView =
        SettingSubtitleView(context, subtitleTextView, subtitleImageView)
    private val switchSourceView =
        SwitchSourceView(context, subtitleTextView, danmuView)
    private val keywordBlockView = KeywordBlockView(context, danmuView)

    private val skipPositionView = SkipPositionView(context)
    private val loadingView = LoadingView(context)

    private val controllerBinding = DataBindingUtil.inflate<LayoutPlayerControllerBinding>(
        LayoutInflater.from(context),
        R.layout.layout_player_controller,
        this,
        true
    )

    init {
        addControlComponent(danmuView)
        addControlComponent(subtitleTextView)
        addControlComponent(subtitleImageView)
        addControlComponent(playerTopView)
        addControlComponent(playerBotView)
        addControlComponent(gestureView)
        addControlComponent(playerSettingView)
        addControlComponent(danmuSettingView)
        addControlComponent(subtitleSettingView)
        addControlComponent(switchSourceView)
        addControlComponent(keywordBlockView)
        addControlComponent(skipPositionView)
        addControlComponent(loadingView)

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

    override fun toggleDanmuVisible() {
        danmuView.toggleVis()
    }

    override fun seekTo(timeMs: Long) {
        danmuView.seekTo(timeMs, mControlWrapper.isPlaying())
    }

    override fun switchSubtitleSource() {
        switchSourceView.setSwitchType(isSwitchSubtitle = true)
        mControlWrapper.showSettingView(SettingViewType.SWITCH_SOURCE)
    }

    override fun switchDanmuSource() {
        switchSourceView.setSwitchType(isSwitchSubtitle = false)
        mControlWrapper.showSettingView(SettingViewType.SWITCH_SOURCE)
    }

    override fun allowSendDanmu(): Boolean {
        return danmuView.allowSendDanmu()
    }

    override fun addDanmuToView(danmuBean: SendDanmuBean) {
        danmuView.addDanmuToView(danmuBean)
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

    fun setVideoTitle(title: String?) {
        playerTopView.setVideoTitle(title)
    }

    fun setVideoFolder(folderPath: String?) {
        switchSourceView.setDefaultFolder(folderPath)
    }

    fun setDanmuPath(url: String?) {
        danmuView.loadDanmu(url)
    }

    fun setSubtitlePath(url: String?) {
        if (url.isNullOrEmpty())
            return
        subtitleSettingView.setSubtitlePath(url)
    }

    fun setLastPosition(position: Long) {
        skipPositionView.setSkipPosition(position)
    }

    fun updateSubtitle(subtitle: MixedSubtitle) {
        subtitleSettingView.updateSubtitle(subtitle)
    }

    fun setBatteryChanged(percent: Int) {
        for (component in mControlComponents) {
            val view = component.key
            if (view is BatteryObserver) {
                view.onBatteryChange(percent)
                break
            }
        }
    }

    fun observerPlayError(block: () -> Unit) {
        mPlayErrorBlock = block
    }

    fun observerPlayExit(block: () -> Unit) {
        playerTopView.setExitObserver(block)
    }

    fun observerBindSource(block: (sourcePath: String, isSubtitle: Boolean) -> Unit) {
        switchSourceView.setBindSourceObserver(block)
    }

    fun observerSendDanmu(block: (danmuData: SendDanmuBean) -> Unit) {
        playerBotView.setSendDanmuBlock(block)
    }

    fun observerDanmuBlock(
        cloudBlock: LiveData<MutableList<DanmuBlockEntity>>? = null,
        add: ((keyword: String, isRegex: Boolean) -> Unit),
        remove: ((id: Int) -> Unit),
        queryAll: () -> LiveData<MutableList<DanmuBlockEntity>>
    ) {
        danmuView.setCloudBlockLiveData(cloudBlock)
        keywordBlockView.setDatabaseBlock(add, remove, queryAll)
    }
}