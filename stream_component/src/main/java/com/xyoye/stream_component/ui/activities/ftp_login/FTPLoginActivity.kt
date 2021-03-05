package com.xyoye.stream_component.ui.activities.ftp_login

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityFtpLoginBinding
import com.xyoye.stream_component.ui.dialog.FTPLoginDialog

@Route(path = RouteTable.Stream.FTPLogin)
class FTPLoginActivity : BaseActivity<FTPLoginViewModel, ActivityFtpLoginBinding>() {

    @Autowired
    @JvmField
    var editData: MediaLibraryEntity? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            FTPLoginViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_ftp_login

    override fun initView() {
        ARouter.getInstance().inject(this)

        FTPLoginDialog(
            editData,
            addMediaStorage = {
                viewModel.addFTPStorage(editData, it)
            },
            testConnect = {
                viewModel.testConnect(it)
            },
            viewModel.testConnectLiveData
        ).show(this)
    }
}