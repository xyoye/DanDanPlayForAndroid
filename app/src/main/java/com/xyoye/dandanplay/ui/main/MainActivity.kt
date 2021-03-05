package com.xyoye.dandanplay.ui.main

import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.bridge.LoginObserver
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.extension.addFragment
import com.xyoye.common_component.extension.findAndHideFragment
import com.xyoye.common_component.extension.hideFragment
import com.xyoye.common_component.extension.showFragment
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.dandanplay.BR
import com.xyoye.dandanplay.R
import com.xyoye.dandanplay.databinding.ActivityMainBinding
import com.xyoye.data_component.data.LoginData

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>(),
    LoginObserver {
    companion object {
        private const val TAG_FRAGMENT_HOME = "tag_fragment_home"
        private const val TAG_FRAGMENT_MEDIA = "tag_fragment_media"
        private const val TAG_FRAGMENT_PERSONAL = "tag_fragment_personal"
    }

    private lateinit var homeFragment: Fragment
    private lateinit var mediaFragment: Fragment
    private lateinit var personalFragment: Fragment
    private lateinit var previousFragment: Fragment

    private var fragmentTag = ""
    private var touchTime = 0L

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            MainViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_main

    override fun initView() {
        //隐藏返回按钮
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
        }

        //默认显示媒体库页面
        //标题
        title = "媒体库"
        //底部导航栏设置选中
        dataBinding.navigationView.selectedItemId = R.id.navigation_media
        //隐藏所有已添加的fragment，防止如旋转屏幕后导致的屏幕错乱
        supportFragmentManager.findAndHideFragment(
            TAG_FRAGMENT_HOME,
            TAG_FRAGMENT_MEDIA,
            TAG_FRAGMENT_PERSONAL
        )
        //切换到媒体库页面
        switchFragment(TAG_FRAGMENT_MEDIA)

        //设置底部导航栏事件
        dataBinding.navigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    title = "弹弹play"
                    switchFragment(TAG_FRAGMENT_HOME)
                }
                R.id.navigation_media -> {
                    title = "媒体库"
                    switchFragment(TAG_FRAGMENT_MEDIA)
                }
                R.id.navigation_personal -> {
                    title = "个人中心"
                    switchFragment(TAG_FRAGMENT_PERSONAL)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        viewModel.initCloudBlockData()

        if (UserConfig.isUserLoggedIn()) {
            viewModel.reLogin()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - touchTime > 1500) {
                ToastCenter.showToast("再按一次退出应用")
                touchTime = System.currentTimeMillis()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun getLoginLiveData(): MutableLiveData<LoginData> {
        return viewModel.reLoginLiveData
    }

    private fun switchFragment(tag: String) {
        //重复打开当前页面，不进行任何操作
        if (tag == fragmentTag) {
            return
        }

        //隐藏上一个布局，fragmentTag不为空代表上一个布局已存在
        if (fragmentTag.isNotEmpty()) {
            supportFragmentManager.hideFragment(previousFragment)
        }

        when (tag) {
            TAG_FRAGMENT_HOME -> {
                //根据TAG寻找页面
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_HOME)
                if (fragment == null) {
                    //根据TAG无法找到页面，通过路由寻找页面，找到页面则添加
                    getFragment(RouteTable.Anime.HomeFragment)?.also {
                        supportFragmentManager.addFragment(
                            R.id.fragment_container,
                            it,
                            TAG_FRAGMENT_HOME
                        )
                        homeFragment = it
                        previousFragment = it
                        fragmentTag = tag
                    }
                } else {
                    //根据TAG找到页面，显示
                    supportFragmentManager.showFragment(fragment)
                    homeFragment = fragment
                    previousFragment = fragment
                    fragmentTag = tag
                }
            }
            TAG_FRAGMENT_MEDIA -> {
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_MEDIA)
                if (fragment == null) {
                    getFragment(RouteTable.Local.MediaFragment)?.also {
                        supportFragmentManager.addFragment(
                            R.id.fragment_container,
                            it,
                            TAG_FRAGMENT_MEDIA
                        )
                        mediaFragment = it
                        previousFragment = it
                        fragmentTag = tag
                    }
                } else {
                    supportFragmentManager.showFragment(fragment)
                    mediaFragment = fragment
                    previousFragment = fragment
                    fragmentTag = tag
                }
            }
            TAG_FRAGMENT_PERSONAL -> {
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_PERSONAL)
                if (fragment == null) {
                    getFragment(RouteTable.User.PersonalFragment)?.also {
                        supportFragmentManager.addFragment(
                            R.id.fragment_container,
                            it,
                            TAG_FRAGMENT_PERSONAL
                        )
                        personalFragment = it
                        previousFragment = it
                        fragmentTag = tag
                    }
                } else {
                    supportFragmentManager.showFragment(fragment)
                    personalFragment = fragment
                    previousFragment = fragment
                    fragmentTag = tag
                }
            }
            else -> {
                throw RuntimeException("no match fragment")
            }
        }
    }

    private fun getFragment(path: String) =
        ARouter.getInstance()
            .build(path)
            .navigation() as Fragment?
}