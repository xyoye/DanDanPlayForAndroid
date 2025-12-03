package com.xyoye.anime_component.ui.activities.search

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ActivitySearchBinding
import com.xyoye.anime_component.listener.SearchListener
import com.xyoye.anime_component.ui.fragment.search_anime.SearchAnimeFragment
import com.xyoye.anime_component.ui.fragment.search_magnet.SearchMagnetFragment
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard

@Route(path = RouteTable.Anime.Search)
class SearchActivity : BaseActivity<SearchViewModel, ActivitySearchBinding>() {
    @Autowired
    @JvmField
    var animeTitle: String? = null

    @Autowired
    @JvmField
    var searchWord: String? = null

    @Autowired
    @JvmField
    var isSearchMagnet: Boolean = false

    private lateinit var searchAdapter: SearchPageAdapter

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            SearchViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_search

    override fun initView() {
        TheRouter.inject(this)

        searchAdapter = SearchPageAdapter(supportFragmentManager, searchWord)

        dataBinding.viewpager.apply {
            adapter = searchAdapter
            offscreenPageLimit = 2
            currentItem = if (isSearchMagnet) 1 else 0
        }

        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewpager)

        if (!isSearchMagnet) {
            dataBinding.searchEt.postDelayed({
                showKeyboard(dataBinding.searchEt)
            }, 200)
        }
        initListener()
    }

    private fun initListener() {
        dataBinding.backIv.setOnClickListener {
            hideKeyboard(dataBinding.searchEt)
            dataBinding.searchCl.requestFocus()
            finish()
        }

        dataBinding.searchEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(dataBinding.searchEt)
                dataBinding.searchCl.requestFocus()
                search(dataBinding.searchEt.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        dataBinding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val textLength = editable?.length ?: 0
                if (textLength > 0) {
                    if (dataBinding.searchEt.isFocused) {
                        dataBinding.clearTextIv.isVisible = true
                    }
                } else {
                    dataBinding.clearTextIv.isVisible = false
                    clearText()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        dataBinding.searchEt.setOnFocusChangeListener { _, isFocus ->
            val searchText = dataBinding.searchEt.text?.toString() ?: ""
            dataBinding.clearTextIv.isVisible = isFocus && searchText.isNotEmpty()
        }

        dataBinding.searchTv.setOnClickListener {
            hideKeyboard(dataBinding.searchEt)
            dataBinding.searchCl.requestFocus()
            search(dataBinding.searchEt.text.toString())
        }

        dataBinding.clearTextIv.setOnClickListener {
            viewModel.searchText.set("")
            showKeyboard(dataBinding.searchEt)
            clearText()
        }

        dataBinding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                dataBinding.searchEt.hint = when (position) {
                    0 -> getString(R.string.search_anime_hint)
                    1 -> getString(R.string.search_magnet_hint)
                    else -> ""
                }
            }

        })
    }

    private fun search(searchText: String) {
        val tag = "android:switcher:${R.id.viewpager}:${dataBinding.viewpager.currentItem}"
        supportFragmentManager.findFragmentByTag(tag)?.apply {
            (this as SearchListener).search(searchText)
        }
    }

    private fun clearText() {
        val tag = "android:switcher:${R.id.viewpager}:${dataBinding.viewpager.currentItem}"
        supportFragmentManager.findFragmentByTag(tag)?.apply {
            (this as SearchListener).onTextClear()
        }
    }

    fun onSearch(searchText: String) {
        hideKeyboard(dataBinding.searchEt)
        dataBinding.searchCl.requestFocus()
        viewModel.searchText.set(searchText)
    }

    fun hideSearchKeyboard() {
        hideKeyboard(dataBinding.searchEt)
        dataBinding.searchCl.requestFocus()
    }

    inner class SearchPageAdapter(
        fragmentManager: FragmentManager,
        private val searchWord: String?
    ) : FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        private var titles = arrayOf("搜番剧", "搜资源")

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> SearchAnimeFragment.newInstance()
                1 -> SearchMagnetFragment.newInstance(searchWord)
                else -> throw IndexOutOfBoundsException("only 2 fragment, but position : $position")

            }
        }

        override fun getCount() = titles.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
}