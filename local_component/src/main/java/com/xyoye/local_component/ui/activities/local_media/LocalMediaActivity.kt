package com.xyoye.local_component.ui.activities.local_media

import android.view.KeyEvent
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.SheetActionType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityLocalMediaBinding
import com.xyoye.local_component.databinding.ItemMediaFolderBinding
import com.xyoye.local_component.databinding.ItemMediaVideoBinding

@Route(path = RouteTable.Local.LocalMediaStorage)
class LocalMediaActivity : BaseActivity<LocalMediaViewModel, ActivityLocalMediaBinding>() {
    companion object {
        private const val ACTION_BIND_DANMU_AUTO = 1
        private const val ACTION_BIND_DANMU_MANUAL = 2
        private const val ACTION_BIND_SUBTITLE = 3
        private const val ACTION_UNBIND_DANMU = 4
        private const val ACTION_UNBIND_SUBTITLE = 5
        private const val ACTION_SHARE_TV = 6
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            LocalMediaViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_local_media

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "本地媒体库"

        initRv()

        dataBinding.fastPlayBt.setOnClickListener {
            viewModel.fastPlay()
        }

        viewModel.fileLiveData.observe(this) {
            dataBinding.mediaRv.setData(it)
        }

        viewModel.folderLiveData.observe(this) {
            dataBinding.mediaRv.setData(it)
        }

        viewModel.refreshEnableLiveData.observe(this) {
            dataBinding.refreshLayout.isEnabled = it
        }

        viewModel.refreshLiveData.observe(this) { isSuccess ->
            if (dataBinding.refreshLayout.isRefreshing) {
                dataBinding.refreshLayout.isRefreshing = false
            }
            if (!isSuccess) {
                ToastCenter.showError("未找到视频文件")
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
            viewModel.listRoot()
        }
        dataBinding.refreshLayout.isRefreshing = true
        viewModel.listRoot()
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
                addItem<Any, ItemMediaFolderBinding>(R.layout.item_media_folder) {
                    checkType { data, _ -> data is FolderBean }
                    initView { data, _, _ ->
                        data as FolderBean
                        itemBinding.apply {
                            val folderName = getFolderName(data.folderPath)
                            folderTv.setAutoSizeText(folderName)
                            folderTv.setTextColorRes(
                                if (data.isLastPlay) R.color.text_theme else R.color.text_black
                            )

                            val fileCount = "${data.fileCount}视频"
                            fileCountTv.text = fileCount
                            itemLayout.setOnClickListener {
                                viewModel.listFolder(folderName, data.folderPath)
                            }
                        }
                    }
                }

                addItem<Any, ItemMediaVideoBinding>(R.layout.item_media_video) {
                    checkType { data, _ -> data is VideoEntity }
                    initView { data, _, _ ->
                        data as VideoEntity
                        itemBinding.run {
                            titleTv.setTextColorRes(
                                if (data.isLastPlay) R.color.text_theme else R.color.text_black
                            )
                            titleTv.setAutoSizeText(getFileNameNoExtension(data.filePath))
                            durationTv.text = formatDuration(data.videoDuration)
                            if (data.fileId != 0L) {
                                val videoUri = IOUtils.getVideoUri(data.fileId)
                                coverIv.setGlideImage(videoUri, 5)
                            }
                            danmuTipsTv.isVisible = isFileExist(data.danmuPath)
                            subtitleTipsTv.isVisible = isFileExist(data.subtitlePath)

                            itemLayout.setOnClickListener {
                                viewModel.checkPlayParams(data)
                            }
                            moreActionIv.setOnClickListener {
                                showVideoManagerDialog(data)
                            }
                            itemLayout.setOnLongClickListener {
                                showVideoManagerDialog(data)
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showVideoManagerDialog(data: VideoEntity) {
        val actionList = mutableListOf(
            SheetActionBean(
                ACTION_BIND_DANMU_AUTO,
                "自动匹配弹幕",
                R.drawable.ic_bind_danmu_auto
            ),
            SheetActionBean(
                ACTION_BIND_DANMU_MANUAL,
                "手动查找弹幕",
                R.drawable.ic_bind_danmu_manual
            ),
            SheetActionBean(
                ACTION_BIND_SUBTITLE,
                "手动查找字幕",
                R.drawable.ic_bind_subtitle
            )
        )

        if (!data.danmuPath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_DANMU,
                    "移除弹幕绑定",
                    R.drawable.ic_unbind_danmu
                )
            )
        }

        if (!data.subtitlePath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_SUBTITLE,
                    "移除字幕绑定",
                    R.drawable.ic_unbind_subtitle
                )
            )
        }

        BottomActionDialog(actionList, SheetActionType.VERTICAL) {
            when (it) {
                ACTION_BIND_DANMU_AUTO -> viewModel.matchDanmu(data.filePath)
                ACTION_UNBIND_DANMU -> viewModel.removeDanmu(data.filePath)
                ACTION_UNBIND_SUBTITLE -> viewModel.removeSubtitle(data.filePath)
                ACTION_BIND_DANMU_MANUAL -> {
                    ARouter.getInstance()
                        .build(RouteTable.Local.BindDanmu)
                        .withString("videoName", getFileName(data.filePath))
                        .withString("videoPath", data.filePath)
                        .navigation()
                }
                ACTION_BIND_SUBTITLE -> {
                    ARouter.getInstance()
                        .build(RouteTable.Local.BindSubtitle)
                        .withString("videoPath", data.filePath)
                        .navigation()
                }
            }
            return@BottomActionDialog true
        }.show(this)
    }
}