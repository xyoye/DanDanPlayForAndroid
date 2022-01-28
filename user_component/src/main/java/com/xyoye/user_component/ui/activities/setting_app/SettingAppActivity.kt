package com.xyoye.user_component.ui.activities.setting_app

import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivitySettingAppBinding
import com.xyoye.user_component.ui.fragment.AppSettingFragment

@Route(path = RouteTable.User.SettingApp)
class SettingAppActivity : BaseActivity<SettingAppViewModel, ActivitySettingAppBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            SettingAppViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_setting_app

    override fun initView() {
        title = "应用设置"

        val fragment = AppSettingFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, "AppSettingFragment")
            .commit()
    }
}