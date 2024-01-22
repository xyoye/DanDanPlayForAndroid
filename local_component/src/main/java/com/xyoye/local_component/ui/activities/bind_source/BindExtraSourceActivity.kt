package com.xyoye.local_component.ui.activities.bind_source

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.android.material.tabs.TabLayoutMediator
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.collectAtStarted
import com.xyoye.common_component.services.StorageFileProvider
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityBindExtraSourceBinding
import com.xyoye.local_component.ui.dialog.SegmentWordDialog
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

    private val pageAdapter by lazy { BindSourcePageAdapter() }

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
        viewModel.setStorageFile(storageFile)

        dataBinding.viewpager.apply {
            adapter = pageAdapter
            currentItem = if (isSearchDanmu) 0 else 1
        }
        TabLayoutMediator(dataBinding.tabLayout, dataBinding.viewpager) { tab, position ->
            tab.text = pageAdapter.getItemTitle(position)
        }.attach()

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
                viewModel.setSearchText(dataBinding.searchEt.text.toString().trim())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        dataBinding.searchEt.addTextChangedListener(afterTextChanged = {
            val textLength = it?.length ?: 0
            dataBinding.clearTextIv.isVisible = textLength > 0
        })

        dataBinding.searchTv.setOnClickListener {
            hideKeyboard(dataBinding.searchEt)
            dataBinding.searchCl.requestFocus()
            viewModel.setSearchText(dataBinding.searchEt.text.toString().trim())
        }

        dataBinding.clearTextIv.setOnClickListener {
            dataBinding.searchEt.setText("")
            showKeyboard(dataBinding.searchEt)
        }

        dataBinding.videoLayout.itemLayout.setOnClickListener {
            dataBinding.searchEt.setText(storageFile.fileName())
            dataBinding.searchEt.setSelection(storageFile.fileName().length)
            showKeyboard(dataBinding.searchEt)
        }

        dataBinding.videoLayout.moreActionIv.setOnClickListener {
            viewModel.segmentTitle(storageFile)
        }

        dataBinding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dataBinding.searchEt.hint = when (position) {
                    0 -> getString(R.string.tips_search_danmu)
                    1 -> getString(R.string.tips_search_subtitle)
                    else -> ""
                }
            }
        })

        viewModel.storageFileFlow.collectAtStarted(this) {
            VideoItemLayout.initVideoLayout(dataBinding, it)
        }

        viewModel.segmentTitleLiveData.observe(this) {
            SegmentWordDialog(this, it) { searchText ->
                dataBinding.searchEt.setText(searchText)
                dataBinding.searchEt.setSelection(searchText.length)

                hideKeyboard(dataBinding.searchEt)
                dataBinding.searchCl.requestFocus()

                viewModel.setSearchText(searchText)
            }.show()
        }
    }

    inner class BindSourcePageAdapter : FragmentStateAdapter(this) {
        private var titles = arrayOf("搜弹幕", "搜字幕")

        override fun getItemCount(): Int {
            return titles.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> BindDanmuSourceFragment.newInstance()
                1 -> BindSubtitleSourceFragment.newInstance()
                else -> throw IndexOutOfBoundsException("only 2 fragment, but position : $position")
            }
        }

        fun getItemTitle(position: Int): String {
            return titles.getOrNull(position).orEmpty()
        }
    }
}