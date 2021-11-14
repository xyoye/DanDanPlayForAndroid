package com.xyoye.player_component.ui.activities.player

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.view.KeyEvent
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
import com.xyoye.common_component.receiver.BatteryBroadcastReceiver
import com.xyoye.common_component.receiver.HeadsetBroadcastReceiver
import com.xyoye.common_component.receiver.PlayerReceiverListener
import com.xyoye.common_component.receiver.ScreenBroadcastReceiver
import com.xyoye.common_component.source.MediaSource
import com.xyoye.common_component.source.MediaSourceManager
import com.xyoye.common_component.source.media.TorrentMediaSource
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.enums.*
import com.xyoye.player.controller.VideoController
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.utils.PlaySourceListener
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Route(path = RouteTable.Player.PlayerCenter)
class PlayerActivity : BaseActivity<PlayerViewModel, ActivityPlayerBinding>(),
    PlayerReceiverListener, PlaySourceListener {

    //电量广播
    private lateinit var batteryReceiver: BatteryBroadcastReceiver

    //锁屏广播
    private lateinit var screenLockReceiver: ScreenBroadcastReceiver

    //耳机广播
    private lateinit var headsetReceiver: HeadsetBroadcastReceiver

    private lateinit var videoController: VideoController

    private var mediaSource: MediaSource? = null

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

        initListener()

        initPlayerConfig()

        applyPlaySource(MediaSourceManager.getInstance().getSource())
    }

    override fun onPause() {
        dataBinding.danDanPlayer.pause()
        super.onPause()
    }

    override fun onDestroy() {
        beforePlayExit()
        dataBinding.danDanPlayer.release()
        unregisterReceiver()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (dataBinding.danDanPlayer.onBackPressed()) {
            return
        }
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return dataBinding.danDanPlayer.onKeyDown(keyCode, event) or super.onKeyDown(keyCode, event)
    }

    override fun onScreenLocked() {

    }

    override fun onBatteryChanged(status: Int, progress: Int) {
        videoController.setBatteryChanged(progress)
    }

    override fun onHeadsetRemoved() {
        dataBinding.danDanPlayer.pause()
    }

    private fun checkPlayParams(source: MediaSource?): Boolean {
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

    }

    private fun applyPlaySource(newSource: MediaSource?) {
        dataBinding.danDanPlayer.pause()
        dataBinding.danDanPlayer.release()

        mediaSource = newSource
        if (checkPlayParams(mediaSource).not()) {
            return
        }
        initPlayer(mediaSource!!)
        afterInitPlayer()
    }

    private fun initPlayer(source: MediaSource) {
        videoController = VideoController(this)
        dataBinding.danDanPlayer.setController(videoController)

        videoController.apply {
            setVideoTitle(source.getVideoTitle())
            setDanmuPath(source.getDanmuPath())
            setSubtitlePath(source.getSubtitlePath())
            setLastPosition(source.getCurrentPosition())
            //资源切换
            observerSourceAction(this@PlayerActivity)
            //播放错误
            observerPlayError {
                showPlayErrorDialog()
            }
            //退出播放
            observerPlayExit {
                finish()
            }
            //绑定资源
            observerBindSource { sourcePath, isSubtitle ->
                if (isSubtitle) {
                    mediaSource?.setSubtitlePath(sourcePath)
                } else {
                    mediaSource?.setDanmuPath(sourcePath)
                }
                viewModel.bindSource(sourcePath, source.getVideoUrl(), isSubtitle)
            }
            //发送弹幕
            observerSendDanmu {
                viewModel.sendDanmu(source.getEpisodeId(), source.getDanmuPath(), it)
            }
            //弹幕屏蔽
            observerDanmuBlock(
                cloudBlock = viewModel.cloudDanmuBlockLiveData,
                add = { keyword, isRegex -> viewModel.addDanmuBlock(keyword, isRegex) },
                remove = { id -> viewModel.removeDanmuBlock(id) },
                queryAll = { viewModel.localDanmuBlockLiveData }
            )
        }

        dataBinding.danDanPlayer.apply {
            setProgressObserver { position, duration ->
                viewModel.addPlayHistory(mediaSource, position, duration)
            }
            setMediaSource(source)
            start()
        }
    }

    private fun afterInitPlayer() {
        mediaSource ?: return

        //设置本地视频文件的父文件夹，用于选取弹、字幕
        if (mediaSource!!.getMediaType() == MediaType.LOCAL_STORAGE) {
            File(mediaSource!!.getVideoUrl()).parentFile?.absolutePath?.let {
                PlayerInitializer.selectSourceDirectory = it
            }
        }
    }

    private fun registerReceiver() {
        batteryReceiver = BatteryBroadcastReceiver(this)
        screenLockReceiver = ScreenBroadcastReceiver(this)
        headsetReceiver = HeadsetBroadcastReceiver(this)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registerReceiver(screenLockReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        registerReceiver(headsetReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    private fun unregisterReceiver() {
        if (this::batteryReceiver.isInitialized) {
            unregisterReceiver(batteryReceiver)
        }
        if (this::screenLockReceiver.isInitialized) {
            unregisterReceiver(screenLockReceiver)
        }
        if (this::headsetReceiver.isInitialized) {
            unregisterReceiver(headsetReceiver)
        }
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
        val source = mediaSource

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
        val source = mediaSource ?: return
        if (source is TorrentMediaSource) {
            PlayTaskBridge.sendTaskRemoveMsg(source.getPlayTaskId())
        }
    }

    override fun hasNextSource(): Boolean {
        return mediaSource?.hasNextSource() ?: false
    }

    override fun hasPreviousSource(): Boolean {
        return mediaSource?.hasPreviousSource() ?: false
    }

    override fun nextSource() {
        lifecycleScope.launch(Dispatchers.IO) {
            val source = mediaSource?.nextSource()
            if (source == null) {
                ToastCenter.showOriginalToast("下一个播放资源不存在")
                return@launch
            }
            withContext(Dispatchers.Main) {
                applyPlaySource(source)
            }
        }
    }

    override fun previousSource() {
        lifecycleScope.launch(Dispatchers.IO) {
            val source = mediaSource?.previousSource()
            if (source == null) {
                ToastCenter.showOriginalToast("上一个播放资源不存在")
                return@launch
            }
            withContext(Dispatchers.Main) {
                applyPlaySource(source)
            }
        }
    }
}