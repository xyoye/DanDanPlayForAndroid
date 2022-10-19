package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.recyclerview.widget.GridLayoutManager
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.extension.nextItemIndex
import com.xyoye.common_component.extension.previousItemIndex
import com.xyoye.common_component.extension.requestIndexChildFocus
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.info.SettingAction
import com.xyoye.player.info.SettingActionType
import com.xyoye.player.info.SettingItem
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemPlayerSettingBinding
import com.xyoye.player_component.databinding.ItemPlayerSettingTypeBinding
import com.xyoye.player_component.databinding.LayoutPlayerSettingBinding

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/10/11
 *     desc  :
 * </pre>
 */

class PlayerSettingView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutPlayerSettingBinding>(context, attrs, defStyleAttr) {

    //操作项集合
    private val settingItems = generateItems()

    init {
        initRv()
    }

    override fun getLayoutId() = R.layout.layout_player_setting

    override fun getSettingViewType(): SettingViewType {
        return SettingViewType.PLAYER_SETTING
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewShow() {
        settingItems.asSequence()
            .filter { it is SettingItem }
            .forEach { applyItemStatus(it as SettingItem) }
        viewBinding.settingRv.adapter?.notifyDataSetChanged()
    }

    override fun onViewHide() {
        viewBinding.settingRv.focusedChild?.clearFocus()
        viewBinding.settingRv.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //未展示
        if (isSettingShowing().not()) {
            return false
        }

        val handled = handleKeyCode(keyCode)
        if (handled) {
            return true
        }

        val firstIndex = settingItems.indexOfFirst { it is SettingItem }
        if (firstIndex != -1) {
            viewBinding.settingRv.requestIndexChildFocus(firstIndex)
        }
        return true
    }

    private fun initRv() {
        viewBinding.settingRv.apply {
            layoutManager = grid(4).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (settingItems[position] is SettingActionType) {
                            return 4
                        }
                        return 1
                    }
                }
            }

            adapter = buildAdapter {
                addItem<Any, ItemPlayerSettingBinding>(R.layout.item_player_setting) {
                    checkType { data, _ -> data is SettingItem }
                    initView { data, position, _ ->
                        (data as SettingItem).apply {
                            itemBinding.tvSetting.text = display
                            itemBinding.ivSetting.setImageResource(icon)
                            itemBinding.ivSetting.isSelected = selected
                            itemBinding.ivSetting.setOnClickListener {
                                onItemClick(position, this)
                            }
                        }
                    }
                }

                addItem<Any, ItemPlayerSettingTypeBinding>(R.layout.item_player_setting_type) {
                    checkType { data, _ -> data is SettingActionType }
                    initView { data, _, _ ->
                        (data as SettingActionType).apply {
                            itemBinding.tvType.text = display
                        }
                    }
                }

                itemAnimator = null

                addItemDecoration(ItemDecorationSpace(0, dp2px(8)))

                setData(settingItems)
            }
        }
    }

    /**
     * 处理KeyCode事件
     */
    private fun handleKeyCode(keyCode: Int): Boolean {
        val focusedView = viewBinding.settingRv.focusedChild
            ?: return false
        val focusedIndex = viewBinding.settingRv.getChildAdapterPosition(focusedView)
        if (focusedIndex == -1) {
            return false
        }
        val targetIndex = getTargetIndexByKeyCode(keyCode, focusedIndex)
        viewBinding.settingRv.requestIndexChildFocus(targetIndex)
        return true
    }

    /**
     * 根据KeyCode与当前焦点位置，取得目标焦点位置
     */
    private fun getTargetIndexByKeyCode(keyCode: Int, focusedIndex: Int): Int {
        when (keyCode) {
            //左规则
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                return settingItems.previousItemIndex<SettingItem>(focusedIndex)
            }
            //右规则
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                return settingItems.nextItemIndex<SettingItem>(focusedIndex)
            }
            //上、下规则
            //按类型分组后，找到当前焦点所在分组的位置，取上/下一个分组的同样位置
            else -> {
                val focusedItem = settingItems[focusedIndex]
                //item按分类分组
                val groupedItemMap = settingItems.asSequence()
                    .filter { it is SettingItem }
                    .groupBy { (it as SettingItem).action.type }
                //当前焦点所在分组
                val focusedList = groupedItemMap.values
                    .find { it.contains(focusedItem) }
                    ?: return -1
                //当前焦点item在分组的位置
                val focusedIndexInList = focusedList.indexOf(focusedItem)
                val listIndexInMap = groupedItemMap.values.indexOf(focusedList)
                val targetList = if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    //上一列分组，无则取最后一列分组
                    groupedItemMap.values.toList()
                        .getOrNull(listIndexInMap - 1)
                        ?: groupedItemMap.values.last()
                } else {
                    //下一列分组，无则取第一列分组
                    groupedItemMap.values.toList()
                        .getOrNull(listIndexInMap + 1)
                        ?: groupedItemMap.values.first()
                }
                //分组对应焦点位置的Item，无则取最后一个
                val upItem = targetList.getOrNull(focusedIndexInList)
                    ?: targetList.last()
                return settingItems.indexOf(upItem)
            }
        }
    }

    /**
     * 生成Item数据
     */
    private fun generateItems(): List<Any> {
        val items = mutableListOf<Any>()
        SettingAction.values()
            .asSequence()
            .sortedBy {
                it.type.widget
            }.groupBy {
                it.type
            }.entries.forEach {
                items.add(it.key)
                items.addAll(
                    it.value.map { action -> SettingItem(action, action.display, action.icon) }
                )
            }
        return items
    }

    private fun applyItemStatus(item: SettingItem) {
        var selected = false

        when (item.action) {
            SettingAction.VIDEO_ASPECT -> {
                selected = PlayerInitializer.screenScale != VideoScreenScale.SCREEN_SCALE_DEFAULT
            }
            SettingAction.VIDEO_SPEED -> {
                selected =
                    PlayerInitializer.Player.videoSpeed != PlayerInitializer.Player.DEFAULT_SPEED
            }
            SettingAction.DANMU_LOAD -> {
                selected = TextUtils.isEmpty(mControlWrapper.getDanmuUrl()).not()
            }
            SettingAction.DANMU_TIME -> {
                selected =
                    PlayerInitializer.Danmu.offsetPosition != PlayerInitializer.Danmu.DEFAULT_POSITION
            }
            SettingAction.DANMU_STYLE -> {
                selected = PlayerInitializer.Danmu.size != PlayerInitializer.Danmu.DEFAULT_SIZE
                        || PlayerInitializer.Danmu.alpha != PlayerInitializer.Danmu.DEFAULT_ALPHA
                        || PlayerInitializer.Danmu.stoke != PlayerInitializer.Danmu.DEFAULT_STOKE
                        || PlayerInitializer.Danmu.speed != PlayerInitializer.Danmu.DEFAULT_SPEED
            }
            SettingAction.SUBTITLE_TIME -> {
                selected =
                    PlayerInitializer.Subtitle.offsetPosition != PlayerInitializer.Subtitle.DEFAULT_POSITION
            }
            SettingAction.SCREEN_ORIENTATION -> {
                selected = PlayerInitializer.isOrientationEnabled
            }
            SettingAction.NEXT_EPISODE -> {
                selected = PlayerInitializer.Player.isAutoPlayNext
            }
            else -> {}
        }
        item.selected = selected
    }

    private fun onItemClick(position: Int, item: SettingItem) {
        when (item.action) {
            SettingAction.SCREEN_ORIENTATION -> {
                val newStatus = !PlayerInitializer.isOrientationEnabled
                PlayerInitializer.isOrientationEnabled = newStatus
                PlayerConfig.putAllowOrientationChange(newStatus)
                item.selected = newStatus
                viewBinding.settingRv.adapter?.notifyItemChanged(position)
            }
            SettingAction.NEXT_EPISODE -> {
                val newStatus = !PlayerInitializer.Player.isAutoPlayNext
                PlayerInitializer.Player.isAutoPlayNext = newStatus
                PlayerConfig.putAutoPlayNext(newStatus)
                item.selected = newStatus
                viewBinding.settingRv.adapter?.notifyItemChanged(position)
            }
            SettingAction.VIDEO_SPEED -> {
                mControlWrapper.showSettingView(SettingViewType.VIDEO_SPEED)
                onSettingVisibilityChanged(false)
            }
            SettingAction.VIDEO_ASPECT -> {
                mControlWrapper.showSettingView(SettingViewType.VIDEO_ASPECT)
                onSettingVisibilityChanged(false)
            }
            SettingAction.AUDIO_STREAM -> {
                mControlWrapper.showSettingView(SettingViewType.AUDIO_STREAM)
                onSettingVisibilityChanged(false)
            }
            SettingAction.DANMU_LOAD -> {
                mControlWrapper.showSettingView(SettingViewType.LOAD_DANMU_SOURCE)
                onSettingVisibilityChanged(false)
            }
            SettingAction.DANMU_CONFIG -> {
                mControlWrapper.showSettingView(SettingViewType.DANMU_CONFIGURE)
                onSettingVisibilityChanged(false)
            }
            SettingAction.DANMU_STYLE -> {
                mControlWrapper.showSettingView(SettingViewType.DANMU_STYLE)
                onSettingVisibilityChanged(false)
            }
            SettingAction.DANMU_TIME -> {
                mControlWrapper.showSettingView(SettingViewType.DANMU_TIME)
                onSettingVisibilityChanged(false)
            }
            SettingAction.SUBTITLE_LOAD -> {
                mControlWrapper.showSettingView(SettingViewType.LOAD_SUBTITLE_SOURCE)
                onSettingVisibilityChanged(false)
            }
            else -> {}
        }
    }
}