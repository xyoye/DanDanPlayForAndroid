package com.xyoye.download_component.ui.activities.play_selection

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.ActivityPlaySelectionBinding
import com.xyoye.download_component.ui.dialog.PlaySelectionDialog
import com.xyoye.download_component.utils.PlayTaskManager

@Route(path = RouteTable.Download.PlaySelection)
class PlaySelectionActivity : BaseActivity<PlaySelectionViewModel, ActivityPlaySelectionBinding>() {

    @Autowired
    @JvmField
    var torrentPath: String? = ""

    @Autowired
    @JvmField
    var torrentTitle: String? = ""

    @Autowired
    @JvmField
    var magnetLink: String? = ""

    @Autowired
    @JvmField
    var torrentFileIndex: Int = -1

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlaySelectionViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_play_selection

    override fun initView() {
        ARouter.getInstance().inject(this)
        title = ""

        initTaskManager()

        if (torrentPath.isNullOrEmpty() && magnetLink.isNullOrEmpty()) {
            finish()
            return
        } else if (!torrentPath.isNullOrEmpty()) {
            showPlaySelectionDialog(torrentPath!!)
        } else {
            viewModel.downloadTorrentFile(magnetLink!!)
        }

        viewModel.torrentDownloadLiveData.observe(this) {
            showPlaySelectionDialog(it)
        }
        viewModel.dismissLiveData.observe(this) {
            finish()
        }

        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()

            finish()
        }
    }

    private fun showPlaySelectionDialog(torrentPath: String) {
        if (torrentFileIndex != -1) {
            viewModel.playWithHistory(torrentPath, torrentFileIndex, torrentTitle)
            return
        }

        PlaySelectionDialog(torrentPath) { selectIndex ->
            viewModel.torrentPlay(torrentPath, selectIndex)
        }.show(this)
    }

    private fun initTaskManager() {
        PlayTaskManager.init()
    }
}