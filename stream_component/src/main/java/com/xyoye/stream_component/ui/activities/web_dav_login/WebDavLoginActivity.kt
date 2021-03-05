package com.xyoye.stream_component.ui.activities.web_dav_login

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityWebDavLoginBinding
import com.xyoye.stream_component.ui.dialog.WebDavLoginDialog

@Route(path = RouteTable.Stream.WebDavLogin)
class WebDavLoginActivity : BaseActivity<WebDavLoginViewModel, ActivityWebDavLoginBinding>() {

    @Autowired
    @JvmField
    var editData: MediaLibraryEntity? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            WebDavLoginViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_web_dav_login

    override fun initView() {
        ARouter.getInstance().inject(this)

        WebDavLoginDialog(
            editData,
            addMediaStorage = {
                viewModel.addWebDavStorage(editData, it)
            },
            testConnect = {
                viewModel.testConnect(it)
            },
            viewModel.testConnectLiveData
        ).show(this)
    }
}