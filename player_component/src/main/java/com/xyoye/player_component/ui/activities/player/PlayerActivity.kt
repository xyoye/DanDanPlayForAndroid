package com.xyoye.player_component.ui.activities.player

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import com.xyoye.common_component.source.media.TorrentMediaSource
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.enums.*
import com.xyoye.player.controller.VideoController
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerBinding
import com.xyoye.player_component.utils.BatteryHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Route(path = RouteTable.Player.PlayerCenter)
class PlayerActivity : BaseActivity<PlayerViewModel, ActivityPlayerBinding>(),
    PlayerReceiverListener {

    private val danmuViewModel: PlayerDanmuViewModel by viewModels()

    //锁屏广播
    private lateinit var screenLockReceiver: ScreenBroadcastReceiver

    //耳机广播
    private lateinit var headsetReceiver: HeadsetBroadcastReceiver

    private lateinit var videoController: VideoController

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

        applyPlaySource(VideoSourceManager.getInstance().getSource())
    }

    override fun onPause() {
        dataBinding.danDanPlayer.pause()
        dataBinding.danDanPlayer.recordPlayInfo()
        super.onPause()
    }

    override fun onDestroy() {
        beforePlayExit()
        unregisterReceiver()
        dataBinding.danDanPlayer.release()
        batteryHelper.release()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (dataBinding.danDanPlayer.onBackPressed()) {
            return
        }
        dataBinding.danDanPlayer.recordPlayInfo()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return dataBinding.danDanPlayer.onKeyDown(keyCode, event) or super.onKeyDown(keyCode, event)
    }

    override fun onScreenLocked() {

    }

    override fun onHeadsetRemoved() {
        dataBinding.danDanPlayer.pause()
    }

    private fun checkPlayParams(source: BaseVideoSource?): Boolean {
        if (source == null || source.getVideoUrl().isEmpty()) {
            CommonDialog.Builder().run {
                content = "解析播放参数失败"
                addPositive("退出重试") {
                    it.dismiss()
                    finish()
                }
                build()
            }.show(this)
            return false
        }

        return true
    }

    private fun initListener() {
        danmuViewModel.loadDanmuLiveData.observe(this) {
            val curVideoSource = dataBinding.danDanPlayer.getVideoSource()
            val curVideoUrl = curVideoSource.getVideoUrl()
            if (curVideoUrl != it.videoUrl){
                return@observe
            }

            videoController.updateLoadDanmuState(it.state)
            if (it.state == LoadDanmuState.MATCH_SUCCESS){
                val danmuPath = it.danmuPath!!
                videoController.showMessage(it.state.msg)
                videoController.setDanmuPath(danmuPath)

                curVideoSource.setDanmuPath(danmuPath)
                curVideoSource.setEpisodeId(it.episodeId)
                viewModel.storeDanmuSourceChange(curVideoSource)
            } else if (it.state == LoadDanmuState.NO_MATCH_REQUIRE) {
                videoController.setDanmuPath(it.danmuPath!!)
            }
        }

        danmuViewModel.downloadDanmuLiveData.observe(this) {
            if (it == null){
                videoController.showMessage("下载弹幕失败")
                return@observe
            }

            val curVideoSource = dataBinding.danDanPlayer.getVideoSource()
            videoController.setDanmuPath(it.first)

            curVideoSource.setDanmuPath(it.first)
            curVideoSource.setEpisodeId(it.second)
            viewModel.storeDanmuSourceChange(curVideoSource)

            videoController.showMessage("加载弹幕成功")
        }
    }

    private fun initPlayer() {
        videoController = VideoController(this)
        dataBinding.danDanPlayer.setController(videoController)

        videoController.apply {
            setBatteryHelper(batteryHelper)

            //播放错误
            observerPlayError {
                showPlayErrorDialog()
            }
            //退出播放
            observerPlayExit {
                dataBinding.danDanPlayer.recordPlayInfo()
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
        }
    }

    private fun applyPlaySource(newSource: BaseVideoSource?) {
        dataBinding.danDanPlayer.recordPlayInfo()
        dataBinding.danDanPlayer.pause()
        dataBinding.danDanPlayer.release()
        videoController.release()

        videoSource = newSource
        if (checkPlayParams(videoSource).not()) {
            return
        }

        updatePlayer(videoSource!!)

        afterInitPlayer()
    }

    private fun updatePlayer(source: BaseVideoSource) {

        videoController.apply {
            setVideoTitle(source.getVideoTitle())
            setLastPosition(source.getCurrentPosition())
        }

        dataBinding.danDanPlayer.apply {
            setVideoSource(source)
            start()
        }

        // TODO: 2021/11/16 逻辑有问题，应该在Player实例化之前就可以执行
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

        //自动匹配弹幕
        if (DanmuConfig.isAutoMatchDanmu() && videoSource!!.getMediaType() != MediaType.FTP_SERVER) {
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
        PlayerInitializer.Player.videoSpeed = PlayerConfig.getVideoSpeed()
        //自动播放下一集
        PlayerInitializer.Player.isAutoPlayNext = PlayerConfig.isAutoPlayNext()

        //VLCPlayer像素格式
        PlayerInitializer.Player.vlcPixelFormat =
            VLCPixelFormat.valueOf(PlayerConfig.getUseVLCPixelFormat())
        PlayerInitializer.Player.vlcHWDecode =
            VLCHWDecode.valueOf(PlayerConfig.getUseVLCHWDecoder())

        //弹幕配置
        PlayerInitializer.Danmu.size = DanmuConfig.getDanmuSize()
        PlayerInitializer.Danmu.speed = DanmuConfig.getDanmuSpeed()
        PlayerInitializer.Danmu.alpha = DanmuConfig.getDanmuAlpha()
        PlayerInitializer.Danmu.stoke = DanmuConfig.getDanmuStoke()
        PlayerInitializer.Danmu.topDanmu = DanmuConfig.isShowTopDanmu()
        PlayerInitializer.Danmu.mobileDanmu = DanmuConfig.isShowMobileDanmu()
        PlayerInitializer.Danmu.bottomDanmu = DanmuConfig.isShowBottomDanmu()
        PlayerInitializer.Danmu.maxLine = DanmuConfig.getDanmuMaxLine()
        PlayerInitializer.Danmu.maxNum = DanmuConfig.getDanmuMaxCount()
        PlayerInitializer.Danmu.cloudBlock = DanmuConfig.isCloudDanmuBlock()
        PlayerInitializer.Danmu.updateInChoreographer = DanmuConfig.isDanmuUpdateInChoreographer()

        //字幕配置
        PlayerInitializer.Subtitle.textSize = (40f * SubtitleConfig.getTextSize() / 100f).toInt()
        PlayerInitializer.Subtitle.strokeWidth =
            (10f * SubtitleConfig.getStrokeWidth() / 100f).toInt()
        PlayerInitializer.Subtitle.textColor = SubtitleConfig.getTextColor()
        PlayerInitializer.Subtitle.strokeColor = SubtitleConfig.getStrokeColor()
    }

    private fun showPlayErrorDialog() {
        val source = videoSource

        val tips = if (source is TorrentMediaSource) {
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

        if (source is TorrentMediaSource) {
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
        if (source is TorrentMediaSource) {
            PlayTaskBridge.sendTaskRemoveMsg(source.getPlayTaskId())
        }
    }

    private fun switchVideoSource(index: Int) {
        showLoading()
        dataBinding.danDanPlayer.pause()
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
}