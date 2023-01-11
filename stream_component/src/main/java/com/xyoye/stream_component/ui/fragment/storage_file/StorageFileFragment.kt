package com.xyoye.stream_component.ui.fragment.storage_file

import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.*
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemStorageFolderBinding
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.danmu
import com.xyoye.common_component.storage.file.subtitle
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.FragmentStorageFileBinding
import com.xyoye.stream_component.ui.activities.storage_file.StorageFileActivity
import com.xyoye.stream_component.utils.storage.StorageFileDiffCallback

class StorageFileFragment :
    BaseFragment<StorageFileFragmentViewModel, FragmentStorageFileBinding>() {

    companion object {

        fun newInstance() = StorageFileFragment()
    }

    private val ownerActivity by lazy {
        requireActivity() as StorageFileActivity
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            StorageFileFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_storage_file

    override fun initView() {
        initRecyclerView()

        viewModel.fileLiveData.observe(this) {
            dataBinding.loading.isVisible = false
            dataBinding.storageFileRv.isVisible = true
            updateStorageFileData(it)
        }
        viewModel.listFile(ownerActivity.storage, ownerActivity.directory)
    }

    fun updateHistory() {
        viewModel.updateHistory(ownerActivity.storage)
    }

    private fun initRecyclerView() {
        dataBinding.storageFileRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addEmptyView(R.layout.layout_empty) {
                    initEmptyView {
                        itemBinding.emptyTv.text = R.string.text_empty_video.toResString()
                    }
                }

                addItem(R.layout.item_storage_folder) {
                    checkType { data -> isDirectoryItem(data) }
                    initView(directoryItem())
                }
                addItem(R.layout.item_storage_video) {
                    checkType { data -> isVideoItem(data) }
                    initView(videoItem())
                }
            }
        }
    }

    private fun isDirectoryItem(data: Any) = data is StorageFile && data.isDirectory()

    private fun isVideoItem(data: Any) = data is StorageFile && data.isFile()

    private fun BaseViewHolderCreator<ItemStorageFolderBinding>.directoryItem() =
        { data: StorageFile ->
            val childFileCount = data.childFileCount()
            val fileCount = if (childFileCount > 0)
                "${childFileCount}文件"
            else
                "目录"
            itemBinding.folderTv.text = data.fileName()
            itemBinding.folderTv.setTextColor(getTitleColor(data))
            itemBinding.fileCountTv.text = fileCount
            itemBinding.itemLayout.setOnClickListener {
                ownerActivity.openDirectory(data)
            }
        }

    private fun BaseViewHolderCreator<ItemStorageVideoBinding>.videoItem() =
        { data: StorageFile ->
            itemBinding.coverIv.loadImage(data)
            itemBinding.titleTv.setTextColor(getTitleColor(data))

            itemBinding.titleTv.text = data.fileName()
            itemBinding.durationTv.text = getProgress(data)
            itemBinding.lastPlayTimeTv.text = getPlayTime(data)

            itemBinding.durationTv.isVisible = isShowDuration(data)
            itemBinding.danmuTipsTv.isVisible = isShowDanmu(data)
            itemBinding.subtitleTipsTv.isVisible = isShowSubtitle(data)
            itemBinding.lastPlayTimeTv.isVisible = isShowLastPlay(data)

            itemBinding.mainActionFl.setOnClickListener {
                ownerActivity.openFile(data)
            }
            itemBinding.moreActionIv.setOnClickListener {
                showMoreAction(data, itemBinding)
            }
            itemBinding.mainActionFl.setOnLongClickListener {
                showMoreAction(data, itemBinding)
                return@setOnLongClickListener true
            }
        }

    private fun getTitleColor(file: StorageFile): Int {
        return when (file.playHistory?.isLastPlay == true) {
            true -> com.xyoye.common_component.R.color.text_theme.toResColor()
            else -> com.xyoye.common_component.R.color.text_black.toResColor()
        }
    }

    private fun getProgress(file: StorageFile): String {
        val position = file.playHistory?.videoPosition ?: 0
        val duration = file.playHistory?.videoDuration ?: 0
        return if (position > 0 && duration > 0) {
            "${formatDuration(position)}/${formatDuration(duration)}"
        } else if (duration > 0) {
            formatDuration(duration)
        } else {
            ""
        }
    }

    private fun getPlayTime(file: StorageFile): String {
        return file.playHistory?.playTime?.run {
            PlayHistoryUtils.formatPlayTime(this)
        } ?: ""
    }

    private fun isShowDuration(file: StorageFile): Boolean {
        return (file.playHistory?.videoDuration ?: 0) > 0
    }

    private fun isShowDanmu(file: StorageFile): Boolean {
        return file.playHistory?.danmuPath?.isNotEmpty() == true
    }

    private fun isShowSubtitle(file: StorageFile): Boolean {
        return file.playHistory?.subtitlePath?.isNotEmpty() == true
    }

    private fun isShowLastPlay(file: StorageFile): Boolean {
        return file.playHistory?.isLastPlay == true
    }

    private fun updateStorageFileData(newData: List<StorageFile>) {
        val adapter = dataBinding.storageFileRv.adapter as BaseAdapter
        val oldData = adapter.items
        val calculateResult = DiffUtil.calculateDiff(
            StorageFileDiffCallback(oldData, newData)
        )
        oldData.clear()
        oldData.addAll(newData)
        calculateResult.dispatchUpdatesTo(adapter)
    }

    private fun showMoreAction(file: StorageFile, binding: ItemStorageVideoBinding) {
        BottomActionDialog(ownerActivity, getMoreActions(file)) {
            when (it.actionId) {
                ManageAction.BIND_DANMU,
                ManageAction.BIND_SUBTITLE -> {
                    bindExtraSource(
                        file,
                        createShareOptions(binding),
                        it.actionId == ManageAction.BIND_DANMU
                    )
                }
                ManageAction.UNBIND_DANMU,
                ManageAction.UNBIND_SUBTITLE -> {
                    unbindExtraSource(file, it.actionId == ManageAction.UNBIND_DANMU)
                }
                ManageAction.SCREENCAST -> {
                    ownerActivity.castFile(file)
                }
            }
            return@BottomActionDialog true
        }.show()
    }

    private fun getMoreActions(file: StorageFile) =
        mutableListOf<SheetActionBean>().apply {
            add(ManageAction.SCREENCAST.toAction())
            add(ManageAction.BIND_DANMU.toAction())
            add(ManageAction.BIND_SUBTITLE.toAction())
            if (file.danmu != null) {
                add(ManageAction.UNBIND_DANMU.toAction())
            }
            if (file.subtitle != null) {
                add(ManageAction.UNBIND_SUBTITLE.toAction())
            }
        }

    private fun bindExtraSource(
        file: StorageFile,
        options: ActivityOptionsCompat,
        bindDanmu: Boolean
    ) {
        val mediaType = ownerActivity.storage.library.mediaType
        var videoPath: String? = null
        if (mediaType == MediaType.LOCAL_STORAGE || mediaType == MediaType.EXTERNAL_STORAGE) {
            videoPath = file.fileUrl()
        }
        var coverUrl: String? = null
        val cover = file.uniqueKey().toCoverFile()
        if (file.uniqueKey().toCoverFile().isValid()) {
            coverUrl = cover!!.absolutePath
        }

        ARouter.getInstance()
            .build(RouteTable.Local.BindExtraSource)
            .withBoolean("isSearchDanmu", bindDanmu)
            .withString("videoPath", videoPath)
            .withString("videoTitle", file.fileName())
            .withString("uniqueKey", file.uniqueKey())
            .withString("mediaType", mediaType.value)
            .withString("fileCoverUrl", coverUrl)
            .withOptionsCompat(options)
            .navigation(activity)
    }

    private fun unbindExtraSource(data: StorageFile, unbindDanmu: Boolean) {
        viewModel.unbindExtraSource(ownerActivity.storage, data, unbindDanmu)
    }

    private fun createShareOptions(binding: ItemStorageVideoBinding): ActivityOptionsCompat {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
            ownerActivity,
            Pair(binding.coverIv, binding.coverIv.transitionName),
            Pair(binding.titleTv, binding.titleTv.transitionName)
        )
    }

    private enum class ManageAction(val title: String, val icon: Int) {
        SCREENCAST("投屏", com.xyoye.common_component.R.drawable.ic_video_cast),
        BIND_DANMU("手动查找弹幕", com.xyoye.common_component.R.drawable.ic_bind_danmu_manual),
        BIND_SUBTITLE("手动查找字幕", com.xyoye.common_component.R.drawable.ic_bind_subtitle),
        UNBIND_DANMU("移除弹幕绑定", com.xyoye.common_component.R.drawable.ic_unbind_danmu),
        UNBIND_SUBTITLE("移除字幕绑定", com.xyoye.common_component.R.drawable.ic_unbind_subtitle);

        fun toAction() = SheetActionBean(this, title, icon)
    }
}