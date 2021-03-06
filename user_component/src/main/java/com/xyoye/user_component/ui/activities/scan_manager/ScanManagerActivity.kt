package com.xyoye.user_component.ui.activities.scan_manager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable

import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityScanManagerBinding
import com.xyoye.user_component.ui.fragment.scan_extend.ScanExtendFragment
import com.xyoye.user_component.ui.fragment.scan_filter.ScanFilterFragment

@Route(path = RouteTable.User.ScanManager)
class ScanManagerActivity : BaseActivity<ScanManagerViewModel, ActivityScanManagerBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ScanManagerViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_scan_manager

    override fun initView() {

        title = "扫描目录管理"

        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewpager)
        dataBinding.viewpager.apply {
            adapter = ScanManagerFragmentAdapter(supportFragmentManager)
            offscreenPageLimit = 2
            currentItem = 0
        }
    }

    inner class ScanManagerFragmentAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private var titles = arrayOf("扫描", "屏蔽")

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> ScanExtendFragment.newInstance()
                1 -> ScanFilterFragment.newInstance()
                else -> throw IllegalArgumentException()
            }
        }

        override fun getCount() = titles.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
}