package com.xyoye.user_component.ui.activities.about_us

import android.annotation.SuppressLint
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.startUrlActivity
import com.xyoye.common_component.utils.AppUtils
import com.xyoye.user_component.BR
import com.xyoye.user_component.BuildConfig
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityAboutUsBinding

@Route(path = RouteTable.User.AboutUs)
class AboutUsActivity : BaseActivity<AboutUsViewModel, ActivityAboutUsBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AboutUsViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_about_us

    @SuppressLint("SetTextI18n")
    override fun initView() {

        title = ""

        val describe = "《弹弹play 概念版》是一个本地视频播放器，是弹弹play系列应用安卓平台的实现，致力于视频+弹幕的播放，为您带来愉悦的观影体验。" +
                "\n\n《弹弹play 概念版》亦是一个免费、开源Android项目，遵循Apache 2.0协议。项目仅用于个人学习、研究，不参与任何商业行为。"

        dataBinding.appDescribeTv.text = describe

        dataBinding.versionTv.text = "v${AppUtils.getVersionName()}  ${BuildConfig.BUILD_COMMIT}"

        dataBinding.officialAddressTv.setOnClickListener {
            startUrlActivity("https://dandanplay.com")
        }

        dataBinding.sourceAddressTv.setOnClickListener {
            startUrlActivity("https://github.com/xyoye/DanDanPlayForAndroid")
        }
    }
}