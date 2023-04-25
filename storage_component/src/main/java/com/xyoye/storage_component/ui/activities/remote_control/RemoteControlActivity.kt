package com.xyoye.storage_component.ui.activities.remote_control

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.storage_component.BR
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.ActivityRemoteControlBinding

@Route(path = RouteTable.Stream.RemoteControl)
class RemoteControlActivity : BaseActivity<RemoteControlViewModel, ActivityRemoteControlBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RemoteControlViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_remote_control

    override fun initView() {

        title = "远程遥控器"

        dataBinding.remoteProgressBar.max = 100

        viewModel.vibrateLiveData.observe(this) {
            vibrate()
        }

        viewModel.updatePlayState.observe(this) {
            dataBinding.controlCenterIv.setImageResource(
                if (viewModel.isPlaying.get() == true)
                    R.drawable.ic_remote_pause
                else
                    R.drawable.ic_remote_play
            )
        }

        viewModel.getPlayInfo()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(50)
        }
    }
}