package com.xyoye.local_component.ui.activities.bind_source

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.services.StorageFileProvider
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
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
    lateinit var storageFileProvider: StorageFileProvider

    @Autowired
    @JvmField
    var isSearchDanmu: Boolean = true

    lateinit var storageFile: StorageFile

    override fun initViewModel() = ViewModelInit(
        BR.viewModel, BindExtraSourceViewModel::class.java
    )

    override fun getLayoutId() = R.layout.activity_bind_extra_source

    override fun initView() {
        ARouter.getInstance().inject(this)

        val storageFile = storageFileProvider.getShareStorageFile()
        if (storageFile == null) {
            finish()
            return
        }
        this.storageFile = storageFile
        VideoItemLayout.initVideoLayout(dataBinding, storageFile)

        dataBinding.viewpager.apply {
            adapter = BindSourcePageAdapter(supportFragmentManager)
            currentItem = if (isSearchDanmu) 0 else 1
        }

        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewpager)

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
                childPage()?.search(dataBinding.searchEt.text.toString().trim())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        dataBinding.searchEt.addTextChangedListener(afterTextChanged = {
            val textLength = it?.length ?: 0
            if (textLength > 0) {
                if (dataBinding.searchEt.isFocused) {
                    dataBinding.clearTextIv.isVisible = true
                }
            } else {
                dataBinding.clearTextIv.isVisible = false
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
            dataBinding.searchEt.setText("")
            showKeyboard(dataBinding.searchEt)
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

        viewModel.historyChangedLiveData.observe(this) {
            VideoItemLayout.initVideoLayout(dataBinding, it)
        }
    }

    private fun childPage(index: Int? = null): ExtraSourceListener? {
        val pageIndex = index ?: dataBinding.viewpager.currentItem
        val tag = "android:switcher:${R.id.viewpager}:$pageIndex"
        return supportFragmentManager.findFragmentByTag(tag) as? ExtraSourceListener?
    }

    fun onSourceChanged() {
        viewModel.updateSourceChanged(storageFile)
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
                0 -> BindDanmuSourceFragment.newInstance()
                1 -> BindSubtitleSourceFragment.newInstance()
                else -> throw IndexOutOfBoundsException("only 2 fragment, but position : $position")

            }
        }

        override fun getCount() = titles.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
}