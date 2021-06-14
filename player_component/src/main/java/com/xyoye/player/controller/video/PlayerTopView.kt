package com.xyoye.player.controller.video

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.utils.dp2px
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutPlayerTopBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by xyoye on 2020/11/3.
 */

class PlayerTopView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InterControllerView {

    private val mHideTranslateY = -dp2px(46).toFloat()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val viewBinding = DataBindingUtil.inflate<LayoutPlayerTopBinding>(
        LayoutInflater.from(context),
        R.layout.layout_player_top,
        this,
        true
    )

    private var playExitObserver : (()->Unit)? = null

    private lateinit var mControlWrapper: ControlWrapper

    init {

        viewBinding.backIv.setOnClickListener {
            playExitObserver?.invoke()
        }

        viewBinding.playerSettingsIv.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.PLAYER_SETTING)
        }

        viewBinding.danmuSettingsTv.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.DANMU_SETTING)
        }

        viewBinding.subtitleSettingsIv.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.SUBTITLE_SETTING)
        }
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            //不加延迟会导致动画卡顿
            postDelayed({
                viewBinding.systemTimeTv.text = timeFormat.format(Date())
            }, 100)

            ViewCompat.animate(viewBinding.playerTopLl).translationY(0f).setDuration(300).start()
        } else {
            viewBinding.videoTitleTv.requestFocus()
            ViewCompat.animate(viewBinding.playerTopLl).translationY(mHideTranslateY)
                .setDuration(300).start()
        }
    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {
        //显示状态与锁定状态相反
        onVisibilityChanged(!isLocked)
    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    fun setBatteryChange(percent: Int) {
        viewBinding.batteryPb.progress = percent
        val batteryText = "$percent%"
        viewBinding.batteryTv.text = batteryText
    }

    fun setVideoTitle(title: String?) {
        viewBinding.videoTitleTv.text = title
    }

    fun setExitObserver(block: ()->Unit){
        playExitObserver = block
    }
}