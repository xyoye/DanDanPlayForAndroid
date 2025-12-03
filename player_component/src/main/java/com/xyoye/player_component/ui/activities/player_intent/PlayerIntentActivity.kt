package com.xyoye.player_component.ui.activities.player_intent

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.therouter.TheRouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.decodeUrl
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.isTorrentFile
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerIntentBinding

class PlayerIntentActivity : BaseActivity<PlayerIntentViewModel, ActivityPlayerIntentBinding>() {

    private lateinit var videoUri: Uri
    private lateinit var videoUrl: String

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlayerIntentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_player_intent

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .fullScreen(true)
            .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
            .init()
    }

    override fun initView() {
        observerPlay()

        dataBinding.exitTv.setOnClickListener {
            finish()
        }

        val intentData = intent.data
        if (intentData == null) {
            viewModel.isParseError.set(true)
            return
        }
        videoUri = intentData
        videoUrl = MediaUtils.getPathFromURI(videoUri)
        if (videoUrl.isEmpty()) {
            videoUrl = intentData.toString()
        } else if (videoUrl.startsWith("file://")) {
            videoUrl = videoUrl.decodeUrl()
            viewModel.addUnrecognizedFile(videoUrl)
        }

        if (isTorrentIntent(videoUrl, intentData, intent.type)) {
            openTorrentStorage(videoUrl)
            return
        }

        viewModel.openIntentUrl(videoUrl)
    }

    private fun observerPlay() {
        viewModel.playLiveData.observe(this) {
            TheRouter
                .build(RouteTable.Player.Player)
                .navigation()
            finish()
        }
    }

    private fun isTorrentIntent(url: String, uri: Uri, mimeType: String?): Boolean {
        if (mimeType == "application/x-bittorrent" || mimeType == "application/torrent") {
            return true
        }
        if (isTorrentFile(url)) {
            return true
        }
        val uriText = uri.toString()
        return isTorrentFile(uriText) || isTorrentFile(uri.lastPathSegment ?: "")
    }

    private fun openTorrentStorage(url: String) {
        if (url.startsWith("file://").not()) {
            return
        }

        val filePath = url.runCatching { toUri().toFile().absolutePath }.getOrNull()
        if (filePath.isNullOrEmpty()) {
            return
        }

        val library = MediaLibraryEntity.TORRENT.copy(url = filePath)
        TheRouter
            .build(RouteTable.Stream.StorageFile)
            .withParcelable("storageLibrary", library)
            .navigation()
        finish()
    }
}