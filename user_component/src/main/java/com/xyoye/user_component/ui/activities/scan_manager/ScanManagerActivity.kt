package com.xyoye.user_component.ui.activities.scan_manager

import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.therouter.router.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityScanManagerBinding
import com.xyoye.user_component.ui.dialog.VideoExtensionSupportSettingDialog
import com.xyoye.user_component.ui.fragment.scan_extend.ScanExtendFragment
import com.xyoye.user_component.ui.fragment.scan_filter.ScanFilterFragment

@Route(path = RouteTable.User.ScanManager)
class ScanManagerActivity : BaseActivity<ScanManagerViewModel, ActivityScanManagerBinding>() {

    private var extensionSettingItem: MenuItem? = null
    private var extensionSettingDialog: VideoExtensionSupportSettingDialog? = null

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

        dataBinding.viewpager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                extensionSettingItem?.isVisible = position == 0
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scan_manager, menu)
        extensionSettingItem = menu.findItem(R.id.item_video_extension_support_setting)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == extensionSettingItem?.itemId) {
            showScanSettingDialog()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showScanSettingDialog() {
        extensionSettingDialog?.dismiss()
        extensionSettingDialog = VideoExtensionSupportSettingDialog(this).also {
            it.show()
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