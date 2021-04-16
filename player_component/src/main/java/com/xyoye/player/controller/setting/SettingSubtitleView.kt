package com.xyoye.player.controller.setting

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.utils.TrackHelper
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
) : LinearLayout(context, attrs, defStyleAttr), InterSettingView {
    private val mHideTranslateX = dp2px(300).toFloat()

    private lateinit var mControlWrapper: ControlWrapper

    private val subtitleTrackList = mutableListOf<VideoTrackBean>()

    private val viewBinding = DataBindingUtil.inflate<LayoutSettingSubtitleBinding>(
        LayoutInflater.from(context),
        R.layout.layout_setting_subtitle,
        this,
        true
    )

    init {
        gravity = Gravity.END

        initSettingView()

        initSettingListener()

        subtitleTrackList.add(TrackUtils.getEmptyTrack())

        TrackHelper.subtitleTrackData.observe(context as LifecycleOwner, Observer { it ->
            var hasExSubtitleSelected = false
            val iterator = subtitleTrackList.iterator()
            while (iterator.hasNext()) {
                val track = iterator.next()
                if (!track.isExTrack) {
                    iterator.remove()
                } else if (track.isChecked) {
                    hasExSubtitleSelected = true
                }
            }

            val innerSelectedTrack = it.find { it.isChecked }
            //有外挂字幕已选中 且 有内置字幕选中，取消内置字幕的选中
            if (hasExSubtitleSelected && innerSelectedTrack != null) {
                mControlWrapper.selectTrack(null, null)
                return@Observer
            }

            //有内置字幕被选中，开启内置字幕显示
            if (innerSelectedTrack != null) {
                mControlWrapper.showInnerTextSubtitle()
                mControlWrapper.setImageSubtitleEnable(true)
            }

            if (it.size > 0) {
                subtitleTrackList.addAll(1, it)
            } else if (subtitleTrackList.size == 1) {
                subtitleTrackList[0].isChecked = true
            }
            viewBinding.subtitleTrackRv.adapter?.notifyDataSetChanged()
        })
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
        mControlWrapper.setDanmuLoadedCallback { sourceUrl, isLoaded ->
            onSubtitleLoaded(sourceUrl, isLoaded)
        }
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    override fun getSettingViewType() = SettingViewType.SUBTITLE_SETTING

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            ViewCompat.animate(viewBinding.subtitleSettingNsv)
                .translationX(0f)
                .setDuration(500)
                .start()
        } else {
            ViewCompat.animate(viewBinding.subtitleSettingNsv)
                .translationX(mHideTranslateX)
                .setDuration(500)
                .start()
        }
    }

    override fun isSettingShowing() = viewBinding.subtitleSettingNsv.translationX == 0f

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

            adapter = buildAdapter<VideoTrackBean> {
                initData(subtitleTrackList)

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
            mControlWrapper.updateOffsetTime()
        }

        viewBinding.subtitleExtraTimeAdd.setOnClickListener {
            hideKeyboard(viewBinding.subtitleExtraTimeEt)
            viewBinding.subtitleOffsetTimeLl.requestFocus()
            PlayerInitializer.Subtitle.offsetPosition += 500
            updateOffsetEt()
            mControlWrapper.updateOffsetTime()
        }

        viewBinding.subtitleExtraTimeEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(viewBinding.subtitleExtraTimeEt)
                viewBinding.subtitleOffsetTimeLl.requestFocus()

                val extraTimeText = viewBinding.subtitleExtraTimeEt.text.toString()
                val newOffsetSecond = if (extraTimeText.isEmpty()) 0f else extraTimeText.toFloat()

                PlayerInitializer.Subtitle.offsetPosition = (newOffsetSecond * 1000).toLong()
                updateOffsetEt()
                mControlWrapper.updateOffsetTime()
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
        if (position > subtitleTrackList.size)
            return

        val currentSelectTrack = subtitleTrackList[position]

        //取消上一次已选中的字幕
        var lastSelectedTrack: VideoTrackBean? = null
        for ((index, track) in subtitleTrackList.withIndex()) {
            if (track.isChecked) {
                //当前选中和已选中相同，不再进行后续操作
                if (currentSelectTrack == track)
                    return

                track.isChecked = false
                lastSelectedTrack = track
                viewBinding.subtitleTrackRv.adapter?.notifyItemChanged(index)
                break
            }
        }

        currentSelectTrack.isChecked = true
        viewBinding.subtitleTrackRv.adapter?.notifyItemChanged(position)

        when {
            //选中“无”的字幕流
            TrackUtils.isEmptyTrack(currentSelectTrack) -> {
                mControlWrapper.selectTrack(null, lastSelectedTrack)
                mControlWrapper.setTextSubtitleDisable()
                mControlWrapper.setImageSubtitleEnable(false)
                return
            }
            //选中外挂字幕流
            currentSelectTrack.isExTrack -> {
                mControlWrapper.showExternalTextSubtitle()
                mControlWrapper.setImageSubtitleEnable(false)
                mControlWrapper.selectTrack(null, null)
            }
            //选中内置字幕流
            else -> {
                mControlWrapper.showInnerTextSubtitle()
                mControlWrapper.setImageSubtitleEnable(true)
                mControlWrapper.selectTrack(currentSelectTrack, lastSelectedTrack)
            }
        }
    }

    private fun onSubtitleLoaded(sourceUrl: String, isLoaded: Boolean){
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
        viewBinding.subtitleTrackRv.adapter?.notifyDataSetChanged()

        if (isLoaded) {
            selectTrack(subtitleTrackList.size - 1)
        }
    }
}