package com.xyoye.download_component.ui.activities.play_selection

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.StorageAdapter
import com.xyoye.data_component.enums.MediaType
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.ActivityPlaySelectionBinding
import com.xyoye.download_component.utils.PlayTaskManager

@Route(path = RouteTable.Download.PlaySelection)
class PlaySelectionActivity : BaseActivity<PlaySelectionViewModel, ActivityPlaySelectionBinding>() {

    @Autowired
    @JvmField
    var torrentPath: String? = ""

    @Autowired
    @JvmField
    var magnetLink: String? = ""

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlaySelectionViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_play_selection

    override fun initView() {
        ARouter.getInstance().inject(this)
        title = "资源详情"

        PlayTaskManager.init()

        initRv()

        viewModel.fileLiveData.observe(this) {
            dataBinding.fileRv.setData(it)
        }

        viewModel.finishLiveData.observe(this) {
            hideLoading()
            finish()
        }

        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }

        viewModel.initTorrentFiles(magnetLink, torrentPath)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDirectoryWithHistory()
    }

    private fun initRv() {
        dataBinding.fileRv.apply {

            layoutManager = vertical()

            adapter = StorageAdapter.newInstance(
                this@PlaySelectionActivity,
                MediaType.WEBDAV_SERVER,
                refreshDirectory = { viewModel.refreshDirectoryWithHistory() },
                openFile = { viewModel.playItem(it) },
                openDirectory = { }
            )
        }
    }
}