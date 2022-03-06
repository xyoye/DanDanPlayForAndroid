package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.weight.CenterLayoutManager
import com.xyoye.data_component.bean.VideoSourceBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemVideoSourceBinding
import com.xyoye.player_component.databinding.LayoutSwitchVideoSourceBinding

/**
 * Created by xyoye on 2021/11/28.
 */

@SuppressLint("ClickableViewAccessibility")
class SwitchVideoSourceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSwitchVideoSourceBinding>(context, attrs, defStyleAttr) {

    private var switchVideoSourceBlock: ((Int) -> Unit)? = null

    init {
        viewBinding.clRoot.setOnTouchListener { _, _ -> return@setOnTouchListener true }

        initRv()
    }

    override fun getLayoutId() = R.layout.layout_switch_video_source

    override fun getSettingViewType() = SettingViewType.SWITCH_VIDEO_SOURCE

    override fun getGravity() = Gravity.START

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        super.onSettingVisibilityChanged(isVisible)
        if (isVisible) {
            buildVideoSource()
        }
    }

    fun setSwitchVideoSourceBlock(block: (Int) -> Unit) {
        switchVideoSourceBlock = block
    }

    private fun initRv() {
        viewBinding.sourceRv.apply {
            layoutManager = CenterLayoutManager(context)

            adapter = buildAdapter {
                addItem<VideoSourceBean, ItemVideoSourceBinding>(R.layout.item_video_source) {
                    initView { data, _, _ ->
                        val indexText = "${data.index + 1}"

                        itemBinding.tvIndex.text = indexText
                        itemBinding.tvIndex.setTextColor(getIndexColor(data.isCurrent))
                        itemBinding.tvSourceName.text = data.title
                        itemBinding.tvSourceName.setTextColor(getTitleColor(data.isCurrent))
                        itemBinding.itemLayout.setOnClickListener {
                            if (data.isCurrent.not()) {
                                onSettingVisibilityChanged(false)
                                switchVideoSourceBlock?.invoke(data.index)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildVideoSource() {
        val videoSource = mControlWrapper.getVideoSource()
        val videoSources = mutableListOf<VideoSourceBean>()
        for (index in 0 until videoSource.getGroupSize()) {
            videoSources.add(
                VideoSourceBean(
                    index,
                    videoSource.indexTitle(index),
                    index == videoSource.getGroupIndex()
                )
            )
        }
        viewBinding.sourceRv.setData(videoSources)
        viewBinding.sourceRv.smoothScrollToPosition(videoSource.getGroupIndex())
    }

    private fun getIndexColor(isCurrent: Boolean): Int {
        return if (isCurrent)
            R.color.text_theme.toResColor()
        else
            R.color.text_gray.toResColor()
    }

    private fun getTitleColor(isCurrent: Boolean): Int {
        return if (isCurrent)
            R.color.text_theme.toResColor()
        else
            R.color.text_white_immutable.toResColor()
    }
}