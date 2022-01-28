package com.xyoye.local_component.ui.activities.bind_source

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setVideoCover
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityBindExtraSourceBinding
import com.xyoye.local_component.listener.ExtraSourceListener
import com.xyoye.local_component.ui.fragment.bind_danmu.BindDanmuSourceFragment
import com.xyoye.local_component.ui.fragment.bind_subtitle.BindSubtitleSourceFragment


/**
 * Created by xyoye on 2022/1/24
 */

@Route(path = RouteTable.Local.BindExtraSource)
class BindExtraSourceActivity :
    BaseActivity<BindExtraSourceViewModel, ActivityBindExtraSourceBinding>() {

    @Autowired
    @JvmField
    var videoTitle: String? = null

    @Autowired
    @JvmField
    var fileCoverUrl: String? = null

    @Autowired
    @JvmField
    var uniqueKey: String? = null

    @Autowired
    @JvmField
    var mediaType: String? = null

    @Autowired
    @JvmField
    var videoPath: String? = null

    @Autowired
    @JvmField
    var isSearchDanmu: Boolean = true

    private lateinit var mMediaType: MediaType

    override fun initViewModel() = ViewModelInit(
        BR.viewModel, BindExtraSourceViewModel::class.java
    )

    override fun getLayoutId() = R.layout.activity_bind_extra_source

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (uniqueKey == null || mediaType == null) {
            finish()
            return
        }

        mMediaType = MediaType.fromValue(mediaType!!)

        dataBinding.viewpager.apply {
            adapter = BindSourcePageAdapter(supportFragmentManager)
            currentItem = if (isSearchDanmu) 0 else 1
        }

        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewpager)

        dataBinding.coverIv.setVideoCover(uniqueKey, fileCoverUrl)
        dataBinding.titleTv.text = videoTitle

        initListener()

        viewModel.updateSourceChanged(uniqueKey!!, mMediaType)
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
                childPage()?.search(dataBinding.searchEt.text.toString().trim())
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
            childPage()?.search(dataBinding.searchEt.text.toString().trim())
        }

        dataBinding.clearTextIv.setOnClickListener {
            viewModel.searchText.set("")
            showKeyboard(dataBinding.searchEt)
        }

        dataBinding.sourceCl.setOnClickListener {
            viewModel.searchText.set(videoTitle)
            showKeyboard(dataBinding.searchEt)
        }

        dataBinding.danmuTipsTv.setOnClickListener {
            childPage(0)?.unbindDanmu()
        }

        dataBinding.subtitleTipsTv.setOnClickListener {
            childPage(1)?.unbindSubtitle()
        }

        dataBinding.settingTv.setOnClickListener {
            childPage()?.setting()
        }

        dataBinding.localFileBt.setOnClickListener {
            childPage()?.localFile()
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
                    0 -> getString(R.string.tips_search_danmu)
                    1 -> getString(R.string.tips_search_subtitle)
                    else -> ""
                }
            }

        })
    }

    private fun childPage(index: Int? = null): ExtraSourceListener? {
        val pageIndex = index ?: dataBinding.viewpager.currentItem
        val tag = "android:switcher:${R.id.viewpager}:$pageIndex"
        return supportFragmentManager.findFragmentByTag(tag) as? ExtraSourceListener?
    }

    fun onSourceChanged() {
        viewModel.updateSourceChanged(uniqueKey!!, mMediaType)
    }

    inner class BindSourcePageAdapter(
        fragmentManager: FragmentManager
    ) : FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        private var titles = arrayOf("搜弹幕", "搜字幕")

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> BindDanmuSourceFragment.newInstance(videoPath, uniqueKey!!, mediaType!!)
                1 -> BindSubtitleSourceFragment.newInstance(videoPath, uniqueKey!!, mediaType!!)
                else -> throw IndexOutOfBoundsException("only 2 fragment, but position : $position")

            }
        }

        override fun getCount() = titles.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
}