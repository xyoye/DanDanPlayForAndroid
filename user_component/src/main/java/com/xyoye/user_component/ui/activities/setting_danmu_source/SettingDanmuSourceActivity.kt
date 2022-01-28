package com.xyoye.user_component.ui.activities.setting_danmu_source

import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivitySettingDanmuSourceBinding
import com.xyoye.user_component.ui.fragment.DanmuSourceSettingFragment

@Route(path = RouteTable.User.SettingDanmuSource)
class SettingDanmuSourceActivity :
    BaseActivity<SettingDanmuSourceViewModel, ActivitySettingDanmuSourceBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            SettingDanmuSourceViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_setting_danmu_source

    override fun initView() {
        title = "弹幕源设置"

        val fragment = DanmuSourceSettingFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, "DanmuSourceSettingFragment")
            .commit()
    }
}