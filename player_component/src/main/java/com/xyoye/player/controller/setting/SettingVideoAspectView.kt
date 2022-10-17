package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.data_component.bean.VideoScaleBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemSeetingVideoAspectBinding
import com.xyoye.player_component.databinding.LayoutSettingVideoAspectBinding

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/10/12
 *     desc  :
 * </pre>
 */

class SettingVideoAspectView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingVideoAspectBinding>(context, attrs, defStyleAttr) {

    private val videoAspectData = mutableListOf(
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_DEFAULT, "默认"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_16_9, "16:9"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_4_3, "4:3"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_ORIGINAL, "原始"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_MATCH_PARENT, "填充"),
        VideoScaleBean(VideoScreenScale.SCREEN_SCALE_CENTER_CROP, "裁剪")
    )

    init {
        initView()
    }

    override fun getLayoutId() = R.layout.layout_setting_video_aspect

    override fun getSettingViewType() = SettingViewType.VIDEO_ASPECT

    override fun onViewShow() {
        applyAspectStatus()
    }

    override fun onViewHide() {
        viewBinding.rvAspect.focusedChild?.clearFocus()
        viewBinding.rvAspect.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        //KeyCode对应的ItemBinding
        val targetItemBinding = findTargetItemBindingByKeyCode(keyCode)
        if (targetItemBinding != null) {
            targetItemBinding.tvName.requestFocus()
            return true
        }

        viewBinding.rvAspect
            .getChildViewBindingAt<ItemSeetingVideoAspectBinding>(0)
            ?.tvName
            ?.requestFocus()
        return true
    }

    private fun initView() {
        viewBinding.rvAspect.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<VideoScaleBean, ItemSeetingVideoAspectBinding>(R.layout.item_seeting_video_aspect) {
                    initView { data, _, _ ->
                        itemBinding.tvName.text = data.scaleName
                        itemBinding.tvName.isSelected = data.isChecked
                        itemBinding.tvName.setOnClickListener {
                            onClickAspect(data.screenScale)
                        }
                    }
                }
            }

            addItemDecoration(
                ItemDecorationOrientation(
                    dividerPx = dp2px(10),
                    headerFooterPx = 0,
                    orientation = RecyclerView.VERTICAL
                )
            )

            setData(videoAspectData)
        }
    }

    private fun applyAspectStatus() {
        val currentAspect = PlayerInitializer.screenScale

        val selectedIndex = videoAspectData.indexOfFirst { it.isChecked }
        if (selectedIndex != -1) {
            if (videoAspectData[selectedIndex].screenScale == currentAspect) {
                return
            }
            videoAspectData[selectedIndex].isChecked = false
            viewBinding.rvAspect.adapter?.notifyItemChanged(selectedIndex)
        }

        val currentIndex = videoAspectData.indexOfFirst { it.screenScale == currentAspect }
        videoAspectData[currentIndex].isChecked = true
        viewBinding.rvAspect.adapter?.notifyItemChanged(currentIndex)
    }

    private fun onClickAspect(aspect: VideoScreenScale) {
        PlayerInitializer.screenScale = aspect
        mControlWrapper.setScreenScale(aspect)
        applyAspectStatus()
    }

    /**
     * 根据KeyCode目标焦点ItemBinding
     */
    private fun findTargetItemBindingByKeyCode(keyCode: Int): ItemSeetingVideoAspectBinding? {
        //已取得焦点的Item
        val focusedChild = viewBinding.rvAspect.focusedChild
            ?: return null
        val focusedChildIndex = viewBinding.rvAspect.getChildAdapterPosition(focusedChild)
        if (focusedChildIndex == -1) {
            return null
        }
        val targetIndex = getTargetIndexByKeyCode(keyCode, focusedChildIndex)
        return viewBinding.rvAspect.getChildViewBindingAt(targetIndex)
    }


    /**
     * 根据KeyCode与当前焦点位置，取得目标焦点位置
     */
    private fun getTargetIndexByKeyCode(keyCode: Int, focusedIndex: Int): Int {
        return when (keyCode) {
            //左、上规则
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> {
                videoAspectData.previousItemIndex<VideoScaleBean>(focusedIndex)
            }
            //右、下规则
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                videoAspectData.nextItemIndex<VideoScaleBean>(focusedIndex)
            }
            else -> {
                -1
            }
        }
    }
}