package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isGone
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.data_component.bean.VideoScaleBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemSettingVideoParamsBinding
import com.xyoye.player_component.databinding.ItemVideoTrackBinding
import com.xyoye.player_component.databinding.LayoutSettingPlayerBinding

/**
 * Created by xyoye on 2020/11/14.
 */

class SettingPlayerView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingPlayerBinding>(context, attrs, defStyleAttr) {

    private val mVideoScaleData = mutableListOf(
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_16_9, "16:9"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_4_3, "4:3"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_ORIGINAL, "原始"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_MATCH_PARENT, "填充"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_CENTER_CROP, "裁剪")
    )

    private val audioTrackData = mutableListOf<VideoTrackBean>()

    init {
        viewBinding.orientationChangeSw.isChecked = PlayerInitializer.isOrientationEnabled
        viewBinding.orientationChangeSw.setOnCheckedChangeListener { _, isChecked ->
            PlayerInitializer.isOrientationEnabled = isChecked
        }

        viewBinding.autoPlayNextSw.isChecked = PlayerInitializer.Player.isAutoPlayNext
        viewBinding.autoPlayNextSw.setOnCheckedChangeListener { _, isChecked ->
            PlayerInitializer.Player.isAutoPlayNext = isChecked
            PlayerConfig.putAutoPlayNext(isChecked)
        }

        for (data in mVideoScaleData) {
            if (data.screenScale == PlayerInitializer.screenScale) {
                data.isChecked = true
                break
            }
        }

        initRv()

        initVideoSpeed()
    }

    override fun getLayoutId() = R.layout.layout_setting_player

    override fun getSettingViewType() = SettingViewType.PLAYER_SETTING

    override fun attach(controlWrapper: ControlWrapper) {
        super.attach(controlWrapper)
        viewBinding.videoSpeedSb.postDelayed({
            viewBinding.videoSpeedSb.value = PlayerInitializer.Player.videoSpeed
        }, 200)
    }

    private fun initRv() {
        viewBinding.videoScaleRv.apply {
            layoutManager = grid(5)

            adapter = buildAdapter {

                addItem<VideoScaleBean, ItemSettingVideoParamsBinding>(R.layout.item_setting_video_params) {
                    initView { data, position, _ ->
                        itemBinding.apply {
                            paramsTv.text = data.scaleName
                            paramsTv.isSelected = data.isChecked
                            paramsTv.setOnClickListener {
                                if (data.isChecked) {
                                    mControlWrapper.setScreenScale(VideoScreenScale.SCREEN_SCALE_DEFAULT)
                                    data.isChecked = false
                                    notifyItemChanged(position)
                                    return@setOnClickListener
                                }

                                for ((index, bean) in mVideoScaleData.withIndex()) {
                                    if (bean.isChecked) {
                                        bean.isChecked = false
                                        notifyItemChanged(index)
                                    }
                                }
                                mControlWrapper.setScreenScale(data.screenScale)
                                data.isChecked = true
                                notifyItemChanged(position)
                            }
                        }
                    }
                }
            }

            setData(mVideoScaleData)
        }

        viewBinding.audioTrackRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<VideoTrackBean, ItemVideoTrackBinding>(R.layout.item_video_track) {
                    initView { data, position, _ ->
                        itemBinding.apply {
                            trackNameTv.text = data.trackName
                            trackSelectCb.isChecked = data.isChecked
                            trackLl.setOnClickListener {
                                selectTrack(position)
                            }
                            trackSelectCb.setOnClickListener {
                                selectTrack(position)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initVideoSpeed() {
        viewBinding.videoSpeedSb.addOnChangeListener { _, value, _ ->
            PlayerConfig.putNewVideoSpeed(value)
            PlayerInitializer.Player.videoSpeed = value

            viewBinding.resetSpeedTv.isGone = value == 1f

            val progressText = "$value"
            viewBinding.videoSpeedTv.text = progressText
            mControlWrapper.setSpeed(value)
        }

        viewBinding.videoSpeedTv.text = "1.0"
        viewBinding.resetSpeedTv.setOnClickListener {
            viewBinding.videoSpeedSb.value = 1f
        }
    }

    fun updateAudioTrack(trackData: MutableList<VideoTrackBean>) {
        audioTrackData.clear()
        audioTrackData.addAll(trackData)
        viewBinding.audioTrackRv.setData(audioTrackData)
    }

    private fun selectTrack(position: Int) {
        if (position > audioTrackData.size)
            return

        var deselect: VideoTrackBean? = null
        for ((index, data) in audioTrackData.withIndex()) {
            if (data.isChecked) {
                //再次选中当前已选中音频流，跳过
                if (index == position)
                    return
                deselect = data
                data.isChecked = false
                viewBinding.audioTrackRv.adapter?.notifyItemChanged(index)
                break
            }
        }

        //直接更新UI
        audioTrackData[position].isChecked = true
        viewBinding.audioTrackRv.adapter?.notifyItemChanged(position)

        mControlWrapper.selectTrack(audioTrackData[position], deselect)
    }
}