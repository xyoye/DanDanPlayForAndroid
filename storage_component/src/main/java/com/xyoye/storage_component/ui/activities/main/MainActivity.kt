package com.xyoye.storage_component.ui.activities.main

import com.xyoye.common_component.base.BaseActivity

import com.xyoye.storage_component.BR
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.ActivityMainBinding

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