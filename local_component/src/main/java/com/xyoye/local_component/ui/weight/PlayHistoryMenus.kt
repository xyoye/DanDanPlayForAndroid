package com.xyoye.local_component.ui.weight

import android.view.Menu
import android.view.MenuItem
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.enums.HistorySort
import com.xyoye.local_component.R
import com.xyoye.local_component.ui.activities.play_history.PlayHistoryActivity
import com.xyoye.local_component.utils.HistorySortOption

/**
 * Created by xyoye on 2023/4/11
 */

class PlayHistoryMenus private constructor(
    private val activity: PlayHistoryActivity,
    menu: Menu
) {

    companion object {
        fun inflater(activity: PlayHistoryActivity, menu: Menu): PlayHistoryMenus {
            activity.menuInflater.inflate(R.menu.menu_history, menu)
            return PlayHistoryMenus(activity, menu)
        }
    }

    private val sortTimeItem = menu.findItem(R.id.action_sort_by_time)
    private val sortNameItem = menu.findItem(R.id.action_sort_by_name)
    private val sortOrderAsc = menu.findItem(R.id.action_sort_order_asc)

    private var onSortChanged: ((HistorySortOption) -> Unit)? = null
    private var onClearHistory: (() -> Unit)? = null

    private val sortOption = HistorySortOption()

    init {
        updateSortItem()
    }

    fun onOptionsItemSelected(item: MenuItem) {
        if (item.itemId == R.id.item_clear_history) {
            showClearConfirmDialog()
            return
        }

        val changed = when (item.itemId) {
            R.id.action_sort_by_time -> sortOption.setSort(HistorySort.TIME)
            R.id.action_sort_by_name -> sortOption.setSort(HistorySort.NAME)
            R.id.action_sort_order_asc -> sortOption.changeAsc()
            else -> false
        }
        if (changed) {
            updateSortItem()
            onSortChanged?.invoke(sortOption)
        }
    }

    private fun updateSortItem() {
        when (sortOption.sort) {
            HistorySort.TIME -> sortTimeItem
            HistorySort.NAME -> sortNameItem
        }.isChecked = true

        sortOrderAsc.isChecked = sortOption.asc
    }

    private fun showClearConfirmDialog() {
        CommonDialog.Builder(activity).run {
            tips = "清空播放记录"
            content = "清空播放记录，将同时移除弹幕和字幕绑定记录，确定清空?"
            addNegative()
            addPositive {
                it.dismiss()
                onClearHistory?.invoke()
            }
            build()
        }.show()
    }

    fun onClearHistory(block: () -> Unit) {
        onClearHistory = block
    }

    fun onSortTypeChanged(block: (HistorySortOption) -> Unit) {
        onSortChanged = block
    }
}