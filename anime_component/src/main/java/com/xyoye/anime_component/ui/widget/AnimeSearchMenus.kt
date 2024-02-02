package com.xyoye.anime_component.ui.widget

import android.view.Menu
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.xyoye.anime_component.R
import com.xyoye.common_component.extension.toResString

/**
 * Created by xyoye on 2024/2/2
 */

class AnimeSearchMenus private constructor(
    private val activity: AppCompatActivity,
    menu: Menu
) {

    companion object {
        fun inflater(activity: AppCompatActivity, menu: Menu): AnimeSearchMenus {
            activity.menuInflater.inflate(R.menu.menu_anime_search, menu)
            return AnimeSearchMenus(activity, menu)
        }
    }

    private val searchItem = menu.findItem(R.id.item_search)

    private var mSearchView = searchItem.actionView as SearchView
    private var onTextChanged: ((String) -> Unit)? = null

    init {
        initSearchView()
    }

    private fun initSearchView() {
        mSearchView.apply {
            isIconified = true
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            queryHint = R.string.tips_search_anime.toResString(activity)
            findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)?.textSize = 16f
        }

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    fun handleBackPressed(): Boolean {
        if (mSearchView.isIconified) {
            return false
        }
        mSearchView.onActionViewCollapsed()
        return true
    }

    fun onSearchTextChanged(block: (String) -> Unit) {
        onTextChanged = block
    }
}