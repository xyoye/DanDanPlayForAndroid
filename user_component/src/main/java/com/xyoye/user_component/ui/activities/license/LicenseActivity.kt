package com.xyoye.user_component.ui.activities.license

import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityLicenseBinding
import com.xyoye.user_component.databinding.ItemLicenseBinding

@Route(path = RouteTable.User.License)
class LicenseActivity : BaseActivity<LicenseViewModel, ActivityLicenseBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            LicenseViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_license

    override fun initView() {

        title = "开源许可协议"

        dataBinding.licenseRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<Pair<String, String>> {
                addItem<Pair<String, String>, ItemLicenseBinding>(R.layout.item_license) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            projectTv.text = data.first
                            licenseTv.text = data.second
                        }
                    }
                }
            }
        }

        viewModel.licenseLiveData.observe(this) {
            dataBinding.licenseRv.setData(it)
        }

        viewModel.getLicense()
    }
}