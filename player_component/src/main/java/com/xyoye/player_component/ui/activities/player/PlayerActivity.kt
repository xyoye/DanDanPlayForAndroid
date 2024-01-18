package com.xyoye.player_component.ui.activities.player

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.bridge.PlayTaskBridge
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.receiver.HeadsetBroadcastReceiver
import com.xyoye.common_component.receiver.PlayerReceiverListener
import com.xyoye.common_component.receiver.ScreenBroadcastReceiver
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.common_component.utils.screencast.ScreencastHandler
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.enums.DanmakuLanguage
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.enums.PixelFormat
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.data_component.enums.SurfaceType
import com.xyoye.data_component.enums.VLCAudioOutput
import com.xyoye.data_component.enums.VLCHWDecode
import com.xyoye.data_component.enums.VLCPixelFormat
import com.xyoye.player.DanDanVideoPlayer
import com.xyoye.player.controller.VideoController
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerBinding
import com.xyoye.player_component.utils.BatteryHelper
import com.xyoye.player_component.widgets.popup.PlayerPopupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Route(path = RouteTable.Player.PlayerCenter)
class PlayerActivity : BaseActivity<PlayerViewModel, ActivityPlayerBinding>(),
    PlayerReceiverListener, ScreencastHandler {

    private val danmuViewModel: PlayerDanmuViewModel by lazy {
        ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[PlayerDanmuViewModel::class.java]
    }

    private val videoController: VideoController by lazy {
        VideoController(this)
    }

    private val danDanPlayer: DanDanVideoPlayer by lazy {
        DanDanVideoPlayer(this)
    }

    //悬浮窗
    private val popupManager: PlayerPopupManager by lazy {
        PlayerPopupManager(this)
    }

    //锁屏广播
    private lateinit var screenLockReceiver: ScreenBroadcastReceiver

    //耳机广播
    private lateinit var headsetReceiver: HeadsetBroadcastReceiver

    private var videoSource: BaseVideoSource? = null

    //电量管理
    private var batteryHelper = BatteryHelper()

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlayerViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_player

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .fullScreen(true)
            .hideBar(BarHide.FLAG_HIDE_BAR)
            .init()
    }

    override fun initView() {
        ARouter.getInstance().inject(this)

        registerReceiver()

        initPlayerConfig()

        initPlayer()

        initListener()

        danDanPlayer.setController(videoController)
        dataBinding.playerContainer.removeAllViews()
        dataBinding.playerContainer.addView(danDanPlayer)

        applyPlaySource(VideoSourceManager.getInstance().getSource())
    }

    override fun onResume() {
        super.onResume()

        exitPopupMode()
    }

    override fun onPause() {
        val popupNotShowing = popupManager.isShowing().not()
        val backgroundPlayDisable = PlayerConfig.isBackgroundPlay().not()
        if (popupNotShowing && backgroundPlayDisable) {
            danDanPlayer.pause()
        }
        danDanPlayer.recordPlayInfo()
        super.onPause()
    }

    override fun onDestroy() {
        beforePlayExit()
        unregisterReceiver()
        danDanPlayer.release()
        batteryHelper.release()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (danDanPlayer.onBackPressed()) {
            return
        }
        danDanPlayer.recordPlayInfo()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return danDanPlayer.onKeyDown(keyCode, event) or super.onKeyDown(keyCode, event)
    }

    override fun onScreenLocked() {

    }

    override fun onHeadsetRemoved() {
        danDanPlayer.pause()
    }

    override fun playScreencast(videoSource: BaseVideoSource) {
        lifecycleScope.launch(Dispatchers.Main) {
            applyPlaySource(videoSource)
        }
    }

    private fun checkPlayParams(source: BaseVideoSource?): Boolean {
        if (source == null || source.getVideoUrl().isEmpty()) {
            CommonDialog.Builder(this).run {
                content = "解析播放参数失败"
                addPositive("退出重试") {
                    it.dismiss()
                    finish()
                }
                build()
            }.show()
            return false
        }

        return true
    }

    private fun initListener() {
        danmuViewModel.loadDanmuLiveData.observe(this) {
            val curVideoSource = danDanPlayer.getVideoSource()
            val curVideoUrl = curVideoSource.getVideoUrl()
            if (curVideoUrl != it.videoUrl) {
                return@observe
            }

            // 历史弹幕，直接加载
            if (it.isHistoryData) {
                videoController.setDanmuPath(it.danmuPath)
                return@observe
            }

            // 新匹配到的弹幕，提示并加载弹幕
            videoController.showMessage("匹配弹幕成功")
            videoController.setDanmuPath(it.danmuPath)

            // 更新视频源数据
            curVideoSource.setDanmuPath(it.danmuPath)
            curVideoSource.setEpisodeId(it.episodeId)
            viewModel.storeDanmuSourceChange(curVideoSource)
        }

        danmuViewModel.downloadDanmuLiveData.observe(this) {
            if (it == null) {
                videoController.showMessage("下载弹幕失败")
                return@observe
            }

            val curVideoSource = danDanPlayer.getVideoSource()
            videoController.setDanmuPath(it.danmuPath)

            curVideoSource.setDanmuPath(it.danmuPath)
            curVideoSource.setEpisodeId(it.episodeId)
            viewModel.storeDanmuSourceChange(curVideoSource)

            videoController.showMessage("加载弹幕成功")
        }
    }

    private fun initPlayer() {
        videoController.apply {
            setBatteryHelper(batteryHelper)

            //播放错误
            observerPlayError {
                showPlayErrorDialog()
            }
            //退出播放
            observerExitPlayer {
                danDanPlayer.recordPlayInfo()
                popupManager.dismiss()
                finish()
            }
            //弹幕屏蔽
            observerDanmuBlock(
                cloudBlock = viewModel.cloudDanmuBlockLiveData,
                add = { keyword, isRegex -> viewModel.addDanmuBlock(keyword, isRegex) },
                remove = { id -> viewModel.removeDanmuBlock(id) },
                queryAll = { viewModel.localDanmuBlockLiveData }
            )
            //弹幕搜索
            observerDanmuSearch(
                search = { danmuViewModel.searchDanmu(it) },
                download = { danmuViewModel.downloadDanmu(it) },
                searchResult = { danmuViewModel.danmuSearchLiveData }
            )
            //进入悬浮窗模式
            observerEnterPopupMode {
                enterPopupMode()
                enterTaskBackground()
            }
            //退出悬浮窗模式
            observerExitPopupMode {
                exitPopupMode()
                exitTaskBackground()
            }
        }
    }

    private fun applyPlaySource(newSource: BaseVideoSource?) {
        danDanPlayer.recordPlayInfo()
        danDanPlayer.pause()
        danDanPlayer.release()
        videoController.release()

        videoSource = newSource
        if (checkPlayParams(videoSource).not()) {
            return
        }
        VideoSourceManager.getInstance().setSource(videoSource!!)

        updatePlayer(videoSource!!)

        afterInitPlayer()
    }

    private fun updatePlayer(source: BaseVideoSource) {

        videoController.apply {
            setVideoTitle(source.getVideoTitle())
            setLastPosition(source.getCurrentPosition())
            setLastPlaySpeed(PlayerConfig.getNewVideoSpeed())
        }

        danDanPlayer.apply {
            setVideoSource(source)
            start()
        }

        videoController.setSubtitlePath(source.getSubtitlePath())
        //当弹幕绑定更新，保存变更
        videoController.observeDanmuSourceChanged { danmuPath, episodeId ->
            source.setDanmuPath(danmuPath)
            source.setEpisodeId(episodeId)
            viewModel.storeDanmuSourceChange(source)
        }
        //当字幕绑定更新，保存变更
        videoController.observeSubtitleSourceChanged {
            source.setSubtitlePath(it)
            viewModel.storeSubtitleSourceChange(source)
        }
        //发送弹幕
        videoController.observerSendDanmu {
            viewModel.sendDanmu(source.getEpisodeId(), source.getDanmuPath(), it)
        }

        videoController.setSwitchVideoSourceBlock {
            switchVideoSource(it)
        }
    }

    private fun afterInitPlayer() {
        videoSource ?: return

        //设置本地视频文件的父文件夹，用于选取弹、字幕
        if (videoSource!!.getMediaType() == MediaType.LOCAL_STORAGE) {
            File(videoSource!!.getVideoUrl()).parentFile?.absolutePath?.let {
                PlayerInitializer.selectSourceDirectory = it
            }
        }

        //自动匹配弹幕，弹窗模式不执行匹配
        if (DanmuConfig.isAutoMatchDanmu()
            && videoSource!!.getMediaType() != MediaType.FTP_SERVER
            && popupManager.isShowing().not()
        ) {
            danmuViewModel.loadDanmu(videoSource!!)
        } else {
            videoController.setDanmuPath(videoSource!!.getDanmuPath())
        }
    }

    private fun registerReceiver() {
        screenLockReceiver = ScreenBroadcastReceiver(this)
        headsetReceiver = HeadsetBroadcastReceiver(this)
        registerReceiver(screenLockReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        registerReceiver(headsetReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        batteryHelper.registerReceiver(this)
    }

    private fun unregisterReceiver() {
        if (this::screenLockReceiver.isInitialized) {
            unregisterReceiver(screenLockReceiver)
        }
        if (this::headsetReceiver.isInitialized) {
            unregisterReceiver(headsetReceiver)
        }
        batteryHelper.unregisterReceiver(this)
    }

    private fun initPlayerConfig() {
        //播放器类型
        PlayerInitializer.playerType = PlayerType.valueOf(PlayerConfig.getUsePlayerType())
        //IJKPlayer像素格式
        PlayerInitializer.Player.pixelFormat =
            PixelFormat.valueOf(PlayerConfig.getUsePixelFormat())
        //IJKPlayer硬解码
        PlayerInitializer.Player.isMediaCodeCEnabled = PlayerConfig.isUseMediaCodeC()
        //IJKPlayer H265硬解码
        PlayerInitializer.Player.isMediaCodeCH265Enabled = PlayerConfig.isUseMediaCodeCH265()
        //IJKPlayer OpenSlEs
        PlayerInitializer.Player.isOpenSLESEnabled = PlayerConfig.isUseOpenSlEs()
        //是否使用SurfaceView
        PlayerInitializer.surfaceType =
            if (PlayerConfig.isUseSurfaceView()) SurfaceType.VIEW_SURFACE else SurfaceType.VIEW_TEXTURE
        //视频速度
        PlayerInitializer.Player.videoSpeed = PlayerConfig.getNewVideoSpeed()
        //自动播放下一集
        PlayerInitializer.Player.isAutoPlayNext = PlayerConfig.isAutoPlayNext()

        //VLCPlayer像素格式
        PlayerInitializer.Player.vlcPixelFormat =
            VLCPixelFormat.valueOf(PlayerConfig.getUseVLCPixelFormat())
        PlayerInitializer.Player.vlcHWDecode =
            VLCHWDecode.valueOf(PlayerConfig.getUseVLCHWDecoder())
        PlayerInitializer.Player.vlcAudioOutput =
            VLCAudioOutput.valueOf(PlayerConfig.getUseVLCAudioOutput())

        //弹幕配置
        PlayerInitializer.Danmu.size = DanmuConfig.getDanmuSize()
        PlayerInitializer.Danmu.speed = DanmuConfig.getDanmuSpeed()
        PlayerInitializer.Danmu.alpha = DanmuConfig.getDanmuAlpha()
        PlayerInitializer.Danmu.stoke = DanmuConfig.getDanmuStoke()
        PlayerInitializer.Danmu.topDanmu = DanmuConfig.isShowTopDanmu()
        PlayerInitializer.Danmu.mobileDanmu = DanmuConfig.isShowMobileDanmu()
        PlayerInitializer.Danmu.bottomDanmu = DanmuConfig.isShowBottomDanmu()
        PlayerInitializer.Danmu.maxScrollLine = DanmuConfig.getDanmuScrollMaxLine()
        PlayerInitializer.Danmu.maxTopLine = DanmuConfig.getDanmuTopMaxLine()
        PlayerInitializer.Danmu.maxBottomLine = DanmuConfig.getDanmuBottomMaxLine()
        PlayerInitializer.Danmu.maxNum = DanmuConfig.getDanmuMaxCount()
        PlayerInitializer.Danmu.cloudBlock = DanmuConfig.isCloudDanmuBlock()
        PlayerInitializer.Danmu.updateInChoreographer = DanmuConfig.isDanmuUpdateInChoreographer()
        PlayerInitializer.Danmu.language = DanmakuLanguage.formValue(DanmuConfig.getDanmuLanguage())

        //字幕配置
        PlayerInitializer.Subtitle.textSize = SubtitleConfig.getTextSize()
        PlayerInitializer.Subtitle.strokeWidth = SubtitleConfig.getStrokeWidth()
        PlayerInitializer.Subtitle.textColor = SubtitleConfig.getTextColor()
        PlayerInitializer.Subtitle.strokeColor = SubtitleConfig.getStrokeColor()
    }

    private fun showPlayErrorDialog() {
        val source = videoSource
        val isTorrentSource = source?.getMediaType() == MediaType.MAGNET_LINK

        val tips = if (source is StorageVideoSource && isTorrentSource) {
            val taskLog = PlayTaskBridge.getTaskLog(source.getPlayTaskId())
            "播放失败，资源已失效或暂时无法访问，请尝试切换资源$taskLog"
        } else {
            "播放失败，请尝试更改播放器设置，或者切换其它播放内核"
        }

        val builder = AlertDialog.Builder(this@PlayerActivity)
            .setTitle("错误")
            .setCancelable(false)
            .setMessage(tips)
            .setNegativeButton("退出播放") { dialog, _ ->
                dialog.dismiss()
                this@PlayerActivity.finish()
            }

        if (isTorrentSource) {
            builder.setPositiveButton("播放器设置") { dialog, _ ->
                dialog.dismiss()
                ARouter.getInstance()
                    .build(RouteTable.User.SettingPlayer)
                    .navigation()
                this@PlayerActivity.finish()
            }
        }

        builder.create().show()
    }

    private fun beforePlayExit() {
        val source = videoSource ?: return
        if (source is StorageVideoSource && source.getMediaType() == MediaType.MAGNET_LINK) {
            PlayTaskBridge.sendTaskRemoveMsg(source.getPlayTaskId())
        }
    }

    private fun switchVideoSource(index: Int) {
        showLoading()
        danDanPlayer.pause()
        lifecycleScope.launch(Dispatchers.IO) {
            val targetSource = videoSource?.indexSource(index)
            if (targetSource == null) {
                ToastCenter.showOriginalToast("播放资源不存在")
                return@launch
            }
            withContext(Dispatchers.Main) {
                hideLoading()
                applyPlaySource(targetSource)
            }
        }
    }

    private fun enterPopupMode() {
        if (popupManager.isShowing()) {
            return
        }
        dataBinding.playerContainer.removeAllViews()
        popupManager.show(danDanPlayer)

        danDanPlayer.enterPopupMode()
    }

    private fun exitPopupMode() {
        if (popupManager.isShowing().not()) {
            return
        }
        popupManager.dismiss()

        dataBinding.playerContainer.removeAllViews()
        dataBinding.playerContainer.addView(danDanPlayer)

        danDanPlayer.exitPopupMode()
    }

    private fun enterTaskBackground() {
        moveTaskToBack(true)
    }

    private fun exitTaskBackground() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
    }
}