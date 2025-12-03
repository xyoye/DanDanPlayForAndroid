package com.xyoye.player_component.ui.activities.player_interceptor

import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.therouter.TheRouter
import com.therouter.router.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerInterceptorBinding

@Route(path = RouteTable.Player.Player)
class PlayerInterceptorActivity :
    BaseActivity<PlayerInterceptorViewModel, ActivityPlayerInterceptorBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlayerInterceptorViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_player_interceptor

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .fullScreen(true)
            .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
            .init()
    }

    override fun initView() {
        if (VideoSourceManager.getInstance().getSource() == null) {
            ToastCenter.showError("播放参数错误，无法播放视频")
            finish()
            return
        }

        TheRouter
            .build(RouteTable.Player.PlayerCenter)
            .navigation()
        finish()
    }
}