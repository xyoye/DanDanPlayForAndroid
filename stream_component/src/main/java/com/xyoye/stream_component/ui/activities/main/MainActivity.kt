package com.xyoye.stream_component.ui.activities.main

import com.xyoye.common_component.base.BaseActivity

import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityMainBinding

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            MainViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_main

    override fun initView() {

    }
}