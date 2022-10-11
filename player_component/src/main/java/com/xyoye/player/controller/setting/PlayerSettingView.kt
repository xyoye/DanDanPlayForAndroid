package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.getChildViewBindingAt
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.data_component.enums.SettingViewType
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

    //当前View可处理的事件
    private val handleKeyCodes = listOf(
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT
    )

    init {
        initRv()
    }

    override fun getLayoutId() = R.layout.layout_player_setting

    override fun getSettingViewType(): SettingViewType {
        return SettingViewType.PLAYER_SETTING
    }

    override fun onViewShowed() {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //未展示
        if (isSettingShowing().not()) {
            return false
        }
        //不是上下左右四个操作
        if (handleKeyCodes.contains(keyCode).not()) {
            return false
        }

        //KeyCode对应的ItemBinding
        val targetItemBinding = findTargetItemBindingByKeyCode(keyCode)
        if (targetItemBinding != null) {
            targetItemBinding.ivSetting.requestFocus()
            return true
        }

        //第一个Item获取焦点
        settingItems.firstOrNull { it is SettingItem }?.let {
            return viewBinding.settingRv
                .getChildViewBindingAt<ItemPlayerSettingBinding>(settingItems.indexOf(it))
                ?.ivSetting?.requestFocus()
                ?: false
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

    private fun onItemClick(position: Int, item: SettingItem) {
        item.selected = !item.selected
        viewBinding.settingRv.adapter?.notifyItemChanged(position)
    }

    /**
     * 根据KeyCode目标焦点ItemBinding
     */
    private fun findTargetItemBindingByKeyCode(keyCode: Int): ItemPlayerSettingBinding? {
        //已取得焦点的Item
        val focusedIconIndex = settingItems.indexOfFirst {
            if (it !is SettingItem) {
                return@indexOfFirst false
            }
            return@indexOfFirst viewBinding.settingRv.getChildAt(settingItems.indexOf(it))
                ?.let { view ->
                    DataBindingUtil.getBinding<ItemPlayerSettingBinding>(view)?.ivSetting?.isFocused
                } ?: false
        }
        if (focusedIconIndex == -1) {
            return null
        }
        val targetIndex = getTargetIndexByKeyCode(keyCode, focusedIconIndex)
        return viewBinding.settingRv.getChildViewBindingAt(targetIndex)
    }

    /**
     * 根据KeyCode与当前焦点位置，取得目标焦点位置
     */
    private fun getTargetIndexByKeyCode(keyCode: Int, focusedIndex: Int): Int {
        when (keyCode) {
            //左规则
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                for (index in (focusedIndex - 1) downTo 0) {
                    if (settingItems[index] is SettingItem) {
                        return index
                    }
                }
                return settingItems.indexOfLast { it is SettingItem }
            }
            //右规则
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                for (index in (focusedIndex + 1) until settingItems.size) {
                    if (settingItems[index] is SettingItem) {
                        return index
                    }
                }
                return settingItems.indexOfFirst { it is SettingItem }
            }
            //上、下规则
            //按类型分组后，找到当前焦点所在分组的位置，取上/下一个分组的同样位置
            else -> {
                val focusedItem = settingItems[focusedIndex]
                //item按分类分组
                val groupedItemMap = settingItems.asSequence()
                    .filter { it is SettingItem }
                    .groupBy { (it as SettingItem).type }
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
                items.addAll(it.value.map { action ->
                    SettingItem(action.type, action.display, action.icon)
                })
            }
        return items
    }

}