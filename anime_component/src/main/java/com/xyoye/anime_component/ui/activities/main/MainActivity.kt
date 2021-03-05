package com.xyoye.anime_component.ui.activities.main

import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ActivityMainBinding
import com.xyoye.common_component.base.BaseActivity

/**
 * Created by xyoye on 2020/7/30.
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

        title = "弹弹play"
    }
}