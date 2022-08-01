package com.xyoye.player_component.ui.activities.player_intent

import android.net.Uri
import com.alibaba.android.arouter.launcher.ARouter
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.decodeUrl
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerIntentBinding

class PlayerIntentActivity : BaseActivity<PlayerIntentViewModel, ActivityPlayerIntentBinding>() {

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
        observerPlay()

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
            videoUrl = videoUrl.decodeUrl()
        } else if (videoUrl.startsWith("file://")) {
            videoUrl = videoUrl.decodeUrl()
            viewModel.addUnrecognizedFile(videoUrl)
        }

        viewModel.openIntentUrl(videoUrl)
    }

    private fun observerPlay() {
        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
            finish()
        }
    }
}