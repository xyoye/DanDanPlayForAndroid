package com.xyoye.user_component.ui.activities.main

import android.content.Intent
import com.xyoye.common_component.base.BaseActivity

import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityMainBinding

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

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

        title = "个人中心"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        supportFragmentManager.findFragmentByTag("personal_fragment")
            ?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}