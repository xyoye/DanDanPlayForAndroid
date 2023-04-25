package com.xyoye.storage_component.ui.weight

import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import com.xyoye.data_component.enums.StorageSort
import com.xyoye.storage_component.R
import com.xyoye.storage_component.ui.activities.storage_file.StorageFileActivity
import com.xyoye.storage_component.utils.storage.StorageSortOption

/**
 * Created by xyoye on 2023/3/31.
 */

class StorageFileMenus private constructor(
    private val activity: StorageFileActivity,
    menu: Menu
) {

    companion object {
        fun inflater(activity: StorageFileActivity, menu: Menu): StorageFileMenus {
            activity.menuInflater.inflate(R.menu.menu_storage_file, menu)
            return StorageFileMenus(activity, menu)
        }
    }

    private val searchItem = menu.findItem(R.id.item_search)
    private val sortNameItem = menu.findItem(R.id.action_sort_by_name)
    private val sortSizeItem = menu.findItem(R.id.action_sort_by_size)
    private val sortOrderAsc = menu.findItem(R.id.action_sort_order_asc)
    private val sortDirectoryFirst = menu.findItem(R.id.action_sort_directory_first)

    private var mSearchView = searchItem.actionView as SearchView
    private var onTextChanged: ((String) -> Unit)? = null
    private var onSortChanged: ((StorageSortOption) -> Unit)? = null

    private val sortOption = StorageSortOption()

    init {
        initSearchView()
        updateSortItem()
    }

    private fun initSearchView() {
        mSearchView.apply {
            isIconified = true
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            queryHint = getSearchHintText()
            findViewById<SearchAutoComplete>(R.id.search_src_text)?.textSize = 16f
        }

        mSearchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(keyword: String): Boolean {
                onTextChanged?.invoke(keyword)
                return false
            }
        })
    }

    private fun updateSortItem() {
        when (sortOption.sort) {
            StorageSort.NAME -> sortNameItem
            StorageSort.SIZE -> sortSizeItem
        }.isChecked = true

        sortOrderAsc.isChecked = sortOption.asc
        sortDirectoryFirst.isChecked = sortOption.directoryFirst
    }

    private fun getSearchHintText(): String {
        if (activity.storage.supportSearch()) {
            return "搜索当前媒体库"
        }
        return "搜索当前目录"
    }

    fun onOptionsItemSelected(item: MenuItem) {
        val changed = when (item.itemId) {
            R.id.action_sort_by_name -> sortOption.setSort(StorageSort.NAME)
            R.id.action_sort_by_size -> sortOption.setSort(StorageSort.SIZE)
            R.id.action_sort_order_asc -> sortOption.changeAsc()
            R.id.action_sort_directory_first -> sortOption.changeDirectoryFirst()
            else -> false
        }
        if (changed) {
            updateSortItem()
            onSortChanged?.invoke(sortOption)
        }
    }

    fun onKeyDown(): Boolean {
        if (mSearchView.isIconified) {
            return false
        }
        mSearchView.onActionViewCollapsed()
        return true
    }

    fun onSearchTextChanged(block: (String) -> Unit) {
        onTextChanged = block
    }

    fun onSortTypeChanged(block: (StorageSortOption) -> Unit) {
        onSortChanged = block
    }
}