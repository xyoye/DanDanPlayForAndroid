package com.xyoye.player_component.ui.activities.player_intent

import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.UriUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.enums.MediaType
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerIntentBinding

class PlayerIntentActivity : BaseActivity<PlayerIntentViewModel, ActivityPlayerIntentBinding>() {

    companion object {
        private const val REQUEST_CODE_BIND_DANMU = 1002
    }

    private var playParams: PlayParams? = null

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

        val videoUri = intent.data
        val videoUriText = videoUri?.toString()
        if (videoUriText.isNullOrEmpty()) {
            viewModel.isParseError.set(true)
            return
        }

        val videoTitle = UriUtils.queryVideoTitle(this, videoUri)
            ?: getFileName(videoUriText)

        playParams = PlayParams(
            videoUriText,
            videoTitle,
            null,
            null,
            0,
            0,
            MediaType.OTHER_STORAGE
        )

        CommonDialog.Builder().apply {
            cancelable = false
            touchCancelable = false
            content = "检测到外部打开视频，是否需要绑定弹幕？"
            addPositive("绑定弹幕") {
                it.dismiss()

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

                openPlayer(playParams!!)
            }
        }.build().show(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_BIND_DANMU) {
            if (resultCode == RESULT_OK) {
                val danmuPath = data?.getStringExtra("danmu_path")
                val episodeId = data?.getIntExtra("episode_id", 0) ?: 0
                playParams!!.danmuPath = danmuPath
                playParams!!.episodeId = episodeId
            }
            //绑定弹幕后自动播放
            openPlayer(playParams!!)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openPlayer(playParams: PlayParams) {
        ARouter.getInstance()
            .build(RouteTable.Player.PlayerCenter)
            .withParcelable("playParams", playParams)
            .navigation()
        finish()
    }
}