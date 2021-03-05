package com.xyoye.download_component.ui.activities.download_detail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.ActivityDownloadDetailBinding
import com.xyoye.download_component.ui.fragment.download_files.DownloadFilesFragment
import com.xyoye.download_component.ui.fragment.download_info.DownloadInfoFragment
import com.xyoye.download_component.ui.fragment.download_peers.DownloadPeersFragment
import com.xyoye.download_component.ui.fragment.download_tracker.DownloadTrackerFragment

@Route(path = RouteTable.Download.DownloadDetail)
class DownloadDetailActivity :
    BaseActivity<DownloadDetailViewModel, ActivityDownloadDetailBinding>() {

    @Autowired
    @JvmField
    var infoHash: String? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DownloadDetailViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_download_detail

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "任务详情"

        if (infoHash == null) {
            CommonDialog.Builder().apply {
                content = "获取任务详情失败，请退出重试"
                addPositive("退出") {
                    finish()
                }
            }.build().show(this)
            return
        }

        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewpager)
        dataBinding.viewpager.apply {
            adapter = DownloadDetailAdapter(supportFragmentManager, infoHash!!)
            offscreenPageLimit = 2
            currentItem = 0
        }
    }

    inner class DownloadDetailAdapter(
        fragmentManager: FragmentManager,
        private val infoHash: String
    ) : FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        private var titles = arrayOf("文件", "详情", "tracker", "节点")

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> DownloadFilesFragment.newInstance(infoHash)
                1 -> DownloadInfoFragment.newInstance(infoHash)
                2 -> DownloadTrackerFragment.newInstance(infoHash)
                3 -> DownloadPeersFragment.newInstance(infoHash)
                else -> DownloadFilesFragment.newInstance(infoHash)
            }
        }

        override fun getCount() = titles.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
}