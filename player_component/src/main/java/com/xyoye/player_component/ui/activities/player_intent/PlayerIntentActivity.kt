package com.xyoye.player_component.ui.activities.player_intent

import android.content.Intent
import android.net.Uri
import com.alibaba.android.arouter.launcher.ARouter
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.decodeUrl
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.media.OuterMediaSource
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.UriUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerIntentBinding

class PlayerIntentActivity : BaseActivity<PlayerIntentViewModel, ActivityPlayerIntentBinding>() {

    companion object {
        private const val REQUEST_CODE_BIND_DANMU = 1002
    }

    private lateinit var videoUri: Uri
    private lateinit var videoUrl: String

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlayerIntentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_player_intent

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .fullScreen(true)
            .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
            .init()
    }

    override fun initView() {
        dataBinding.exitTv.setOnClickListener {
            finish()
        }

        val intentData = intent.data
        if (intentData == null) {
            viewModel.isParseError.set(true)
            return
        }
        videoUri = intentData
        videoUrl = MediaUtils.getPathFromURI(videoUri)
        if (videoUrl.isEmpty()) {
            videoUrl = intentData.toString()
        } else if (videoUrl.startsWith("file://")) {
            videoUrl = videoUrl.decodeUrl()
            viewModel.addUnrecognizedFile(videoUrl)
        }

        observeHistory()
        viewModel.queryHistory(videoUrl)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_BIND_DANMU) {
            var danmuPath: String? = null
            var episodeId = 0
            if (resultCode == RESULT_OK) {
                danmuPath = data?.getStringExtra("danmu_path")
                episodeId = data?.getIntExtra("episode_id", 0) ?: 0
            }
            openPlayer(danmuPath, episodeId)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun observeHistory() {
        viewModel.historyLiveData.observe(this) {
            if (it == null || it.danmuPath.isNullOrEmpty()) {
                showSelectDanmuDialog()
            } else {
                openPlayer(it.danmuPath, it.episodeId, it)
            }
        }
    }

    private fun showSelectDanmuDialog() {
        CommonDialog.Builder().apply {
            cancelable = false
            touchCancelable = false
            content = "检测到外部打开视频，是否需要绑定弹幕？"
            addPositive("绑定弹幕") {
                it.dismiss()

                val videoTitle = UriUtils.queryVideoTitle(BaseApplication.getAppContext(), videoUri)
                    ?: getFileName(videoUrl)

                ARouter.getInstance()
                    .build(RouteTable.Local.BindDanmu)
                    .withString("videoName", videoTitle)
                    .navigation(
                        this@PlayerIntentActivity,
                        REQUEST_CODE_BIND_DANMU
                    )
            }
            addNegative("直接播放") {
                it.dismiss()

                openPlayer()
            }
        }.build().show(this)
    }

    private fun openPlayer(
        danmuPath: String? = null,
        episodeId: Int = 0,
        history: PlayHistoryEntity? = null
    ) {
        val videoSource = OuterMediaSource.build(
            videoUrl,
            history,
            danmuPath,
            episodeId
        )
        VideoSourceManager.getInstance().setSource(videoSource)
        ARouter.getInstance()
            .build(RouteTable.Player.Player)
            .navigation()
        finish()
    }
}