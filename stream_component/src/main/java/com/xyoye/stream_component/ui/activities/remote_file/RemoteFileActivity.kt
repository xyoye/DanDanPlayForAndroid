package com.xyoye.stream_component.ui.activities.remote_file

import android.view.KeyEvent
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.RemoteHelper
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.utils.isFileExist
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityRemoteFileBinding
import com.xyoye.stream_component.databinding.ItemRemoteFolderBinding
import com.xyoye.stream_component.databinding.ItemRemoteVideoBinding

@Route(path = RouteTable.Stream.RemoteFile)
class RemoteFileActivity : BaseActivity<RemoteFileViewModel, ActivityRemoteFileBinding>() {

    @Autowired
    @JvmField
    var remoteData: MediaLibraryEntity? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RemoteFileViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_remote_file

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (remoteData == null) {
            ToastCenter.showError("媒体库数据错误，请重试")
            title = "远程媒体库"
            return
        }
        title = remoteData!!.displayName

        initRv()

        viewModel.videoLiveData.observe(this) {
            dataBinding.mediaRv.setData(it)
        }

        viewModel.folderLiveData.observe(this) {
            dataBinding.mediaRv.setData(it)
        }

        viewModel.refreshEnableLiveData.observe(this) {
            dataBinding.refreshLayout.isEnabled = it
        }

        viewModel.refreshLiveData.observe(this) {
            if (dataBinding.refreshLayout.isRefreshing) {
                dataBinding.refreshLayout.isRefreshing = false
            }
        }

        viewModel.playVideoLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .withParcelable("playParams", it)
                .navigation()
        }

        dataBinding.refreshLayout.setColorSchemeResources(R.color.text_theme)
        dataBinding.refreshLayout.setOnRefreshListener {
            viewModel.openStorage(remoteData!!)
        }
        dataBinding.refreshLayout.isRefreshing = true
        viewModel.openStorage(remoteData!!)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!viewModel.inRootFolder.get()) {
                viewModel.listRoot()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initRv() {

        dataBinding.mediaRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<Any> {
                addItem<Any, ItemRemoteFolderBinding>(R.layout.item_remote_folder) {
                    checkType { data, _ -> data is FolderBean }
                    initView { data, _, _ ->
                        data as FolderBean
                        itemBinding.apply {
                            folderTv.setAutoSizeText(data.folderPath)
                            folderTv.setTextColorRes(
                                if (data.isLastPlay) R.color.text_theme else R.color.text_black
                            )

                            val fileCount = "${data.fileCount}视频"
                            fileCountTv.text = fileCount
                            itemLayout.setOnClickListener {
                                viewModel.listFolder(data.folderPath)
                            }
                        }
                    }
                }

                addItem<Any, ItemRemoteVideoBinding>(R.layout.item_remote_video) {
                    checkType { data, _ -> data is RemoteVideoData }
                    initView { data, position, _ ->
                        data as RemoteVideoData
                        itemBinding.run {
                            val videoName = data.EpisodeTitle ?: data.Name
                            titleTv.setAutoSizeText(videoName)
                            durationTv.text = formatDuration(data.Duration * 1000)
                            val coverUrl = RemoteHelper.getInstance().buildImageUrl(data.Hash)
                            coverIv.setGlideImage(coverUrl, 5)

                            danmuTipsTv.isVisible = isFileExist(data.danmuPath)
                            subtitleTipsTv.isVisible = isFileExist(data.subtitlePath)

                            itemLayout.setOnClickListener {
                                viewModel.openVideo(data)
                            }
                        }
                    }
                }
            }
        }
    }
}