package com.xyoye.download_component.ui.activities.play_selection

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.enums.MediaType
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.ActivityPlaySelectionBinding
import com.xyoye.download_component.ui.dialog.PlaySelectionDialog
import java.net.URLDecoder

@Route(path = RouteTable.Download.PlaySelection)
class PlaySelectionActivity : BaseActivity<PlaySelectionViewModel, ActivityPlaySelectionBinding>() {

    companion object {
        private const val PLAY_REQUEST_CODE = 1001
    }

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
                .withParcelable("playParams", it)
                .navigation(this, PLAY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLAY_REQUEST_CODE) {
            viewModel.removePlayTask()
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showPlaySelectionDialog(torrentPath: String) {
        if (torrentFileIndex != -1) {
            val playUrl = viewModel.prepareTorrentPlay(torrentPath, torrentFileIndex)
            if (playUrl == null) {
                finish()
                return
            }
            viewModel.playWithHistory(playUrl, torrentPath, torrentFileIndex, torrentTitle)
            return
        }

        PlaySelectionDialog(torrentPath) { selectIndex ->
            val playUrl = viewModel.prepareTorrentPlay(torrentPath, selectIndex)
            if (playUrl == null) {
                finish()
                return@PlaySelectionDialog
            }
            play(playUrl, torrentPath, selectIndex)
        }.show(this)
    }

    private fun play(playUrl: String, torrentPath: String, torrentFileIndex: Int) {
        var decodedUrl = URLDecoder.decode(playUrl, "utf-8")
        decodedUrl = URLDecoder.decode(decodedUrl, "utf-8")
        val videoTitle = getFileName(decodedUrl)

        val extra = mapOf(
            Pair("torrent_path", torrentPath),
            Pair("torrent_file_index", torrentFileIndex.toString()),
            Pair("torrent_title", torrentTitle ?: "")
        )

        val playParams = PlayParams(
            playUrl,
            videoTitle,
            null,
            null,
            0,
            0,
            MediaType.MAGNET_LINK,
            extra
        )

        ARouter.getInstance()
            .build(RouteTable.Player.Player)
            .withParcelable("playParams", playParams)
            .withString("searchKeyword", torrentTitle)
            .navigation(this, PLAY_REQUEST_CODE)
    }
}