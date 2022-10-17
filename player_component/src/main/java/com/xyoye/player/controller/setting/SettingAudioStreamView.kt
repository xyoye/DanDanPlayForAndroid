package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.data_component.bean.VideoStreamBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemSeetingAudioStreamBinding
import com.xyoye.player_component.databinding.LayoutSettingAudioStreamBinding

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/10/12
 *     desc  :
 * </pre>
 */

class SettingAudioStreamView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingAudioStreamBinding>(context, attrs, defStyleAttr) {

    private val audioStreamData = mutableListOf<VideoStreamBean>()

    init {
        initView()
    }

    override fun getLayoutId() = R.layout.layout_setting_audio_stream

    override fun getSettingViewType() = SettingViewType.AUDIO_STREAM

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewShow() {
        audioStreamData.clear()
        audioStreamData.addAll(mControlWrapper.getAudioStream())
        viewBinding.rvStream.setData(audioStreamData)
    }

    override fun onViewHide() {
        viewBinding.rvStream.focusedChild?.clearFocus()
        viewBinding.rvStream.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        val handled = handleKeyCode(keyCode)
        if (handled) {
            return true
        }

        if (audioStreamData.size > 0) {
            viewBinding.rvStream.requestIndexChildFocus(0)
        }
        return true
    }

    private fun initView() {
        viewBinding.rvStream.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<VideoStreamBean, ItemSeetingAudioStreamBinding>(R.layout.item_seeting_audio_stream) {
                    initView { data, _, _ ->
                        itemBinding.tvName.text = data.trackName
                        itemBinding.tvName.isSelected = data.isChecked
                        itemBinding.tvName.setOnClickListener {
                            onClickStream(data)
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

    private fun onClickStream(stream: VideoStreamBean) {
        val selectedIndex = audioStreamData.indexOfFirst { it.isChecked }
        if (selectedIndex != -1) {
            if (audioStreamData[selectedIndex].trackId == stream.trackId
                && audioStreamData[selectedIndex].trackGroupId == stream.trackGroupId
            ) {
                return
            }
            audioStreamData[selectedIndex].isChecked = false
            viewBinding.rvStream.adapter?.notifyItemChanged(selectedIndex)
        }

        val currentIndex = audioStreamData.indexOfFirst {
            it.trackId == stream.trackId && it.trackGroupId == stream.trackGroupId
        }
        audioStreamData[currentIndex].isChecked = true
        viewBinding.rvStream.adapter?.notifyItemChanged(currentIndex)

        mControlWrapper.selectStream(stream)
    }

    /**
     * 处理KeyCode事件
     */
    private fun handleKeyCode(keyCode: Int): Boolean {
        //已取得焦点的Item
        val focusedChild = viewBinding.rvStream.focusedChild
            ?: return false
        val focusedChildIndex = viewBinding.rvStream.getChildAdapterPosition(focusedChild)
        if (focusedChildIndex == -1) {
            return false
        }
        val targetIndex = getTargetIndexByKeyCode(keyCode, focusedChildIndex)
        viewBinding.rvStream.requestIndexChildFocus(targetIndex)
        return true
    }


    /**
     * 根据KeyCode与当前焦点位置，取得目标焦点位置
     */
    private fun getTargetIndexByKeyCode(keyCode: Int, focusedIndex: Int): Int {
        return when (keyCode) {
            //左、上规则
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> {
                audioStreamData.previousItemIndex<VideoStreamBean>(focusedIndex)
            }
            //右、下规则
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                audioStreamData.nextItemIndex<VideoStreamBean>(focusedIndex)
            }
            else -> {
                -1
            }
        }
    }
}