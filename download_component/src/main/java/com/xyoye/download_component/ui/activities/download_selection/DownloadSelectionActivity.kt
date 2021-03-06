package com.xyoye.download_component.ui.activities.download_selection

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable

import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.ActivityDownloadSelectionBinding
import com.xyoye.download_component.ui.dialog.DownloadSelectionDialog

@Route(path = RouteTable.Download.DownloadSelection)
class DownloadSelectionActivity :
    BaseActivity<DownloadSelectionViewModel, ActivityDownloadSelectionBinding>() {

    @Autowired
    @JvmField
    var torrentPath: String? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DownloadSelectionViewModel::class.java
        )

    override fun initStatusBar() {

    }

    override fun getLayoutId() = R.layout.activity_download_selection

    override fun initView() {
        ARouter.getInstance().inject(this)
        title = ""

        DownloadSelectionDialog(torrentPath){
            if (it != null){
                ARouter.getInstance()
                    .build(RouteTable.Download.DownloadList)
                    .withString("torrentPath", torrentPath)
                    .withByteArray("selection", boolean2byte(it))
                    .navigation()
            }
        }.show(this)
    }

    private fun boolean2byte(booleanList: MutableList<Boolean>): ByteArray{
        val byteArray = ByteArray(booleanList.size)
        for ((index, value) in booleanList.withIndex()) {
            byteArray[index] = if (value) 1 else 0
        }
        return byteArray
    }
}