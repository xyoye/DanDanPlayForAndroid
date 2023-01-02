package com.xyoye.stream_component.ui.fragment.storage_file

import androidx.core.view.isVisible
import com.xyoye.common_component.adapter.BaseViewHolderCreator
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.databinding.ItemStorageFolderBinding
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.FragmentStorageFileBinding
import com.xyoye.stream_component.ui.activities.storage_file.StorageFileActivity
import com.xyoye.common_component.storage.file.StorageFile

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
            dataBinding.storageFileRv.setData(it)
        }
        viewModel.listFile(ownerActivity.storage, ownerActivity.directory)
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
            itemBinding.coverIv.setVideoCover(data.uniqueKey(), data.fileUrl())
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

    private fun showMoreAction(data: StorageFile, binding: ItemStorageVideoBinding) {
        // TODO: 更多操作
    }
}