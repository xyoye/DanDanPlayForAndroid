package com.xyoye.user_component.ui.activities.setting_player

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.therouter.router.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivitySettingPlayerBinding
import com.xyoye.user_component.ui.fragment.DanmuSettingFragment
import com.xyoye.user_component.ui.fragment.PlayerSettingFragment
import com.xyoye.user_component.ui.fragment.SubtitleSettingFragment

@Route(path = RouteTable.User.SettingPlayer)
class SettingPlayerActivity :
    BaseActivity<SettingPlayerViewModel, ActivitySettingPlayerBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            SettingPlayerViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_setting_player

    override fun initView() {

        title = "播放器设置"

        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewpager)
        dataBinding.viewpager.apply {
            adapter = SettingFragmentAdapter(supportFragmentManager)
            offscreenPageLimit = 2
            currentItem = 0
        }
    }

    inner class SettingFragmentAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private var titles = arrayOf("视频", "弹幕", "字幕")

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> PlayerSettingFragment.newInstance()
                1 -> DanmuSettingFragment.newInstance( )
                2 -> SubtitleSettingFragment.newInstance()
                else -> throw IllegalArgumentException()
            }
        }

        override fun getCount() = titles.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
}