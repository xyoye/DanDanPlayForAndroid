package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.nextItemIndex
import com.xyoye.common_component.extension.previousItemIndex
import com.xyoye.common_component.extension.requestIndexChildFocus
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.common_component.weight.CenterLayoutManager
import com.xyoye.data_component.bean.VideoSourceBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemVideoSourceBinding
import com.xyoye.player_component.databinding.LayoutSwitchVideoSourceBinding

/**
 * Created by xyoye on 2021/11/28.
 */

class SwitchVideoSourceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSwitchVideoSourceBinding>(context, attrs, defStyleAttr) {

    private var switchVideoSourceBlock: ((Int) -> Unit)? = null
    private val mVideoSources = mutableListOf<VideoSourceBean>()

    init {
        initRv()
    }

    override fun getLayoutId() = R.layout.layout_switch_video_source

    override fun getSettingViewType() = SettingViewType.SWITCH_VIDEO_SOURCE

    override fun getGravity() = Gravity.START

    override fun onViewShow() {
        buildVideoSource()
    }

    override fun onViewHide() {
        viewBinding.sourceRv.focusedChild?.clearFocus()
        viewBinding.sourceRv.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        val handled = handleKeyCode(keyCode)
        if (handled) {
            return true
        }

        val currentIndex = mVideoSources.indexOfFirst { it.isCurrent }
        if (currentIndex != -1) {
            viewBinding.sourceRv.requestIndexChildFocus(currentIndex)
        } else if (mVideoSources.size > 0) {
            viewBinding.sourceRv.requestIndexChildFocus(0)
        }
        return true
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
                        itemBinding.tvSourceName.isSelected = data.isCurrent
                        itemBinding.tvSourceName.text = data.title
                        itemBinding.tvSourceName.setOnClickListener {
                            if (data.isCurrent.not()) {
                                onSettingVisibilityChanged(false)
                                switchVideoSourceBlock?.invoke(data.index)
                            }
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
        }
    }

    private fun buildVideoSource() {
        mVideoSources.clear()

        val videoSource = mControlWrapper.getVideoSource()
        for (index in 0 until videoSource.getGroupSize()) {
            mVideoSources.add(
                VideoSourceBean(
                    index,
                    videoSource.indexTitle(index),
                    index == videoSource.getGroupIndex()
                )
            )
        }
        viewBinding.sourceRv.setData(mVideoSources)
        viewBinding.sourceRv.smoothScrollToPosition(videoSource.getGroupIndex())
    }

    /**
     * 处理KeyCode事件
     */
    private fun handleKeyCode(keyCode: Int): Boolean {
        //已取得焦点的Item
        val focusedChild = viewBinding.sourceRv.focusedChild
            ?: return false
        val focusedChildIndex = viewBinding.sourceRv.getChildAdapterPosition(focusedChild)
        if (focusedChildIndex == -1) {
            return false
        }
        val targetIndex = getTargetIndexByKeyCode(keyCode, focusedChildIndex)
        viewBinding.sourceRv.requestIndexChildFocus(targetIndex)
        return true
    }


    /**
     * 根据KeyCode与当前焦点位置，取得目标焦点位置
     */
    private fun getTargetIndexByKeyCode(keyCode: Int, focusedIndex: Int): Int {
        return when (keyCode) {
            //左、上规则
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> {
                mVideoSources.previousItemIndex<VideoSourceBean>(focusedIndex)
            }
            //右、下规则
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                mVideoSources.nextItemIndex<VideoSourceBean>(focusedIndex)
            }
            else -> {
                -1
            }
        }
    }
}