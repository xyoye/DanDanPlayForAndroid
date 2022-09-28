package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.utils.TrackUtils
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemVideoTrackBinding
import com.xyoye.player_component.databinding.LayoutSettingSubtitleBinding

/**
 * Created by xyoye on 2020/12/15.
 */

class SettingSubtitleView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingSubtitleBinding>(context, attrs, defStyleAttr) {

    private val subtitleTrackList = mutableListOf<VideoTrackBean>()

    init {
        initSettingView()

        initSettingListener()
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
        mControlWrapper.setSubtitleLoadedCallback { sourceUrl, isLoaded ->
            onSubtitleLoaded(sourceUrl, isLoaded)
        }
    }

    override fun getLayoutId() = R.layout.layout_setting_subtitle

    override fun getSettingViewType() = SettingViewType.SUBTITLE_SETTING

    fun updateSubtitleTrack(trackData: MutableList<VideoTrackBean>) {
        subtitleTrackList.clear()
        subtitleTrackList.add(TrackUtils.getEmptyTrack())
        subtitleTrackList.addAll(trackData)

        val hasInnerSubtitleChecked = trackData.any { it.isChecked }

        //有内置字幕被选中，开启内置字幕显示
        if (hasInnerSubtitleChecked) {
            mControlWrapper.showInnerTextSubtitle()
            mControlWrapper.setImageSubtitleEnable(true)
        } else {
            subtitleTrackList[0].isChecked = true
        }

        viewBinding.subtitleTrackRv.setData(subtitleTrackList)
    }

    private fun initSettingView() {
        //文字大小
        val textSizePercent = (PlayerInitializer.Subtitle.textSize / 40f * 100).toInt()
        val textSizeText = "$textSizePercent%"
        viewBinding.subtitleSizeTv.text = textSizeText
        viewBinding.subtitleSizeSb.progress = textSizePercent

        //描边宽度
        val strokeWidthPercent = (PlayerInitializer.Subtitle.strokeWidth / 10f * 100).toInt()
        val strokeWidthText = "$strokeWidthPercent%"
        viewBinding.subtitleStrokeWidthTv.text = strokeWidthText
        viewBinding.subtitleStrokeWidthSb.progress = strokeWidthPercent

        //文字颜色
        val textColor = PlayerInitializer.Subtitle.textColor
        viewBinding.subtitleColorSb.seekToColor(textColor)
        val textColorText = "${viewBinding.subtitleColorSb.position}%"
        viewBinding.subtitleColorTv.text = textColorText

        //描边颜色
        val strokeColor = PlayerInitializer.Subtitle.strokeColor
        viewBinding.subtitleStrokeColorSb.seekToColor(strokeColor)
        viewBinding.subtitleStrokeWidthTv.text = strokeWidthText
        val strokeColorText = "${viewBinding.subtitleStrokeColorSb.position}%"
        viewBinding.subtitleStrokeColorTv.text = strokeColorText

        //字幕时间调节
        val extraPosition = PlayerInitializer.Subtitle.offsetPosition / 1000f
        viewBinding.subtitleExtraTimeEt.setText(extraPosition.toString())

        viewBinding.subtitleTrackRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {

                addItem<VideoTrackBean, ItemVideoTrackBinding>(R.layout.item_video_track) {
                    initView { data, position, _ ->
                        itemBinding.apply {
                            var trackType = data.trackName
                            if (!TrackUtils.isEmptyTrack(data)) {
                                trackType =
                                    if (data.isExTrack) "[外挂]$trackType" else "[内置]$trackType"
                            }

                            trackNameTv.text = trackType
                            trackNameTv.setTextColorRes(
                                if (data.isDisable) R.color.text_gray else R.color.text_white_immutable
                            )

                            trackSelectCb.isChecked = data.isChecked
                            trackSelectCb.isEnabled = !data.isDisable

                            trackLl.setOnClickListener {
                                if (data.isDisable)
                                    return@setOnClickListener
                                selectTrack(position)
                            }
                            trackSelectCb.setOnClickListener {
                                if (data.isDisable)
                                    return@setOnClickListener
                                selectTrack(position)
                            }
                        }
                    }
                }
            }

            setData(subtitleTrackList)
        }
    }

    private fun initSettingListener() {
        viewBinding.subtitleSizeSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressText = "$progress%"
                viewBinding.subtitleSizeTv.text = progressText

                SubtitleConfig.putTextSize(progress)
                val size = (40f * progress / 100f).toInt()
                PlayerInitializer.Subtitle.textSize = size
                mControlWrapper.updateTextSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        viewBinding.subtitleStrokeWidthSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressText = "$progress%"
                viewBinding.subtitleStrokeWidthTv.text = progressText

                SubtitleConfig.putStrokeWidth(progress)
                val strokeWidth = (10f * progress / 100f).toInt()
                PlayerInitializer.Subtitle.strokeWidth = strokeWidth
                mControlWrapper.updateStrokeWidth()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        viewBinding.subtitleColorSb.setOnColorChangeListener { position, color ->
            val progressText = "$position%"
            viewBinding.subtitleColorTv.text = progressText

            SubtitleConfig.putTextColor(color)
            PlayerInitializer.Subtitle.textColor = color
            mControlWrapper.updateTextColor()
        }

        viewBinding.subtitleStrokeColorSb.setOnColorChangeListener { position, color ->
            val progressText = "$position%"
            viewBinding.subtitleStrokeColorTv.text = progressText

            SubtitleConfig.putStrokeColor(color)
            PlayerInitializer.Subtitle.strokeColor = color
            mControlWrapper.updateStrokeColor()
        }

        viewBinding.addSubtitleTrackTv.setOnClickListener {
            onSettingVisibilityChanged(false)
            mControlWrapper.switchSource(true)
        }

        viewBinding.subtitleExtraTimeReduce.setOnClickListener {
            hideKeyboard(viewBinding.subtitleExtraTimeEt)
            viewBinding.subtitleOffsetTimeLl.requestFocus()
            PlayerInitializer.Subtitle.offsetPosition -= 500
            updateOffsetEt()
            mControlWrapper.updateSubtitleOffsetTime()
        }

        viewBinding.subtitleExtraTimeAdd.setOnClickListener {
            hideKeyboard(viewBinding.subtitleExtraTimeEt)
            viewBinding.subtitleOffsetTimeLl.requestFocus()
            PlayerInitializer.Subtitle.offsetPosition += 500
            updateOffsetEt()
            mControlWrapper.updateSubtitleOffsetTime()
        }

        viewBinding.subtitleExtraTimeEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(viewBinding.subtitleExtraTimeEt)
                viewBinding.subtitleOffsetTimeLl.requestFocus()

                val extraTimeText = viewBinding.subtitleExtraTimeEt.text.toString()
                val newOffsetSecond = if (extraTimeText.isEmpty()) 0f else extraTimeText.toFloat()

                PlayerInitializer.Subtitle.offsetPosition = (newOffsetSecond * 1000).toLong()
                updateOffsetEt()
                mControlWrapper.updateSubtitleOffsetTime()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun updateOffsetEt() {
        val offsetSecond = PlayerInitializer.Subtitle.offsetPosition / 1000f
        viewBinding.subtitleExtraTimeEt.setText(offsetSecond.toString())
    }

    private fun selectTrack(position: Int) {
        val targetTrack = subtitleTrackList.getOrNull(position) ?: return
        if (targetTrack.isChecked) {
            return
        }

        val currentTrackIndex = subtitleTrackList.indexOfFirst { it.isChecked }
        if (currentTrackIndex != -1) {
            subtitleTrackList[currentTrackIndex].isChecked = false
            viewBinding.subtitleTrackRv.adapter?.notifyItemChanged(currentTrackIndex)
        }

        subtitleTrackList[position].isChecked = true
        viewBinding.subtitleTrackRv.adapter?.notifyItemChanged(position)

        when {
            //选中“无”的字幕流
            TrackUtils.isEmptyTrack(targetTrack) -> {
                mControlWrapper.selectTrack(null, subtitleTrackList[currentTrackIndex])
                mControlWrapper.setTextSubtitleDisable()
                mControlWrapper.setImageSubtitleEnable(false)
                return
            }
            //选中外挂字幕流
            targetTrack.isExTrack -> {
                mControlWrapper.showExternalTextSubtitle()
                mControlWrapper.setImageSubtitleEnable(false)
                mControlWrapper.selectTrack(null, null)
            }
            //选中内置字幕流
            else -> {
                mControlWrapper.showInnerTextSubtitle()
                mControlWrapper.setImageSubtitleEnable(true)
                mControlWrapper.selectTrack(targetTrack, subtitleTrackList[currentTrackIndex])
            }
        }
    }

    private fun onSubtitleLoaded(sourceUrl: String, isLoaded: Boolean) {
        //添加显示外挂字幕前，移除之前所有外挂字幕
        val iterator = subtitleTrackList.iterator()
        while (iterator.hasNext()) {
            val track = iterator.next()
            if (track.isExTrack && track.mSourceUrl != null) {
                iterator.remove()
            }
        }

        subtitleTrackList.add(
            TrackUtils.buildExSubtitleTrack(sourceUrl, isDisable = !isLoaded)
        )
        viewBinding.subtitleTrackRv.setData(subtitleTrackList)

        if (isLoaded) {
            selectTrack(subtitleTrackList.size - 1)
        }
    }
}