package com.xyoye.local_component.ui.activities.main

import com.xyoye.common_component.base.BaseActivity
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityMainBinding

/**
 * Created by xyoye on 2020/7/29.
 */

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override fun initViewModel() =
        ViewModelInit(BR.viewModel, MainViewModel::class.java)

    override fun getLayoutId() = R.layout.activity_main

    override fun initView() {
        //隐藏返回按钮
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
        }

        title = "媒体库"
    }
}