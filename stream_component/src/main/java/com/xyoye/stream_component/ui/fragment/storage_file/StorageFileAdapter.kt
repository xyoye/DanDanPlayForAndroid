package com.xyoye.stream_component.ui.fragment.storage_file

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.*
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemStorageFolderBinding
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.databinding.ItemStorageVideoTagBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.danmu
import com.xyoye.common_component.storage.file.subtitle
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.bean.VideoTagBean
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.R
import com.xyoye.stream_component.ui.activities.storage_file.StorageFileActivity

/**
 * Created by xyoye on 2023/4/13
 */

object StorageFileAdapter {

    private enum class ManageAction(val title: String, val icon: Int) {
        SCREENCAST("投屏", com.xyoye.common_component.R.drawable.ic_video_cast),
        BIND_DANMU("手动查找弹幕", com.xyoye.common_component.R.drawable.ic_bind_danmu_manual),
        BIND_SUBTITLE("手动查找字幕", com.xyoye.common_component.R.drawable.ic_bind_subtitle),
        UNBIND_DANMU("移除弹幕绑定", com.xyoye.common_component.R.drawable.ic_unbind_danmu),
        UNBIND_SUBTITLE("移除字幕绑定", com.xyoye.common_component.R.drawable.ic_unbind_subtitle);

        fun toAction() = SheetActionBean(this, title, icon)
    }

    private val tagDecoration = ItemDecorationOrientation(5.dp(), 0, RecyclerView.HORIZONTAL)

    fun create(activity: StorageFileActivity, viewModel: StorageFileFragmentViewModel): BaseAdapter {
        return buildAdapter {
            addEmptyView(R.layout.layout_empty) {
                initEmptyView {
                    itemBinding.emptyTv.text = R.string.text_empty_video.toResString()
                }
            }

            addItem(R.layout.item_storage_folder) {
                checkType { data -> isDirectoryItem(data) }
                initView(directoryItem(activity))
            }
            addItem(R.layout.item_storage_video) {
                checkType { data -> isVideoItem(data) }
                initView(videoItem(activity, viewModel))
            }
        }
    }


    private fun isDirectoryItem(data: Any) = data is StorageFile && data.isDirectory()

    private fun isVideoItem(data: Any) = data is StorageFile && data.isFile()

    private fun BaseViewHolderCreator<ItemStorageFolderBinding>.directoryItem(activity: StorageFileActivity) =
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
                activity.openDirectory(data)
            }
        }

    private fun BaseViewHolderCreator<ItemStorageVideoBinding>.videoItem(
        activity: StorageFileActivity,
        viewModel: StorageFileFragmentViewModel
    ) =
        { data: StorageFile ->
            itemBinding.coverIv.loadImage(data)
            itemBinding.titleTv.setTextColor(getTitleColor(data))

            itemBinding.titleTv.text = data.fileName()

            itemBinding.durationTv.text = getDuration(data)
            itemBinding.durationTv.isVisible = isShowDuration(data)

            setupVideoTag(itemBinding.tagRv, data)

            itemBinding.mainActionFl.setOnClickListener {
                activity.openFile(data)
            }
            itemBinding.moreActionIv.setOnClickListener {
                showMoreAction(data, itemBinding, activity, viewModel)
            }
            itemBinding.mainActionFl.setOnLongClickListener {
                showMoreAction(data, itemBinding, activity, viewModel)
                return@setOnLongClickListener true
            }
        }

    private fun setupVideoTag(tagRv: RecyclerView, data: StorageFile) {
        tagRv.apply {
            layoutManager = horizontal()
            adapter = buildAdapter {
                addItem(R.layout.item_storage_video_tag) { initView(tagItem()) }
            }
            removeItemDecoration(tagDecoration)
            addItemDecoration(tagDecoration)
            setData(generateVideoTags(data))
        }
    }

    private fun BaseViewHolderCreator<ItemStorageVideoTagBinding>.tagItem() =
        { data: VideoTagBean ->
            val background = R.drawable.background_video_tag.toResDrawable()
            background?.colorFilter = PorterDuffColorFilter(data.color, PorterDuff.Mode.SRC)
            itemBinding.textView.background = background
            itemBinding.textView.text = data.tag
        }

    private fun generateVideoTags(data: StorageFile): List<VideoTagBean> {
        val tagList = mutableListOf<VideoTagBean>()
        if (isShowDanmu(data)) {
            tagList.add(VideoTagBean("弹幕", R.color.theme.toResColor()))
        }
        if (isShowSubtitle(data)) {
            tagList.add(VideoTagBean("字幕", R.color.orange.toResColor()))
        }
        val progress = getProgress(data)
        if (progress.isNotEmpty()) {
            tagList.add(VideoTagBean(progress, R.color.gray_40.toResColor()))
        }
        val lastPlayTime = getPlayTime(data)
        if (lastPlayTime.isNotEmpty()) {
            tagList.add(VideoTagBean(lastPlayTime, R.color.gray_40.toResColor()))
        }
        return tagList
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
        if (position == 0L || duration == 0L) {
            return ""
        }

        var progress = (position * 100f / duration).toInt()
        if (progress == 0) {
            progress = 1
        }
        return "进度 $progress%"
    }

    private fun getDuration(file: StorageFile): String {
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

    private fun showMoreAction(
        file: StorageFile,
        binding: ItemStorageVideoBinding,
        activity: StorageFileActivity,
        viewModel: StorageFileFragmentViewModel,
    ) {
        BottomActionDialog(activity, getMoreActions(file)) {
            when (it.actionId) {
                ManageAction.BIND_DANMU,
                ManageAction.BIND_SUBTITLE -> {
                    bindExtraSource(
                        file,
                        it.actionId == ManageAction.BIND_DANMU,
                        createShareOptions(binding, activity),
                        activity,
                    )
                }
                ManageAction.UNBIND_DANMU,
                ManageAction.UNBIND_SUBTITLE -> {
                    viewModel.unbindExtraSource(file, it.actionId == ManageAction.UNBIND_DANMU)
                }
                ManageAction.SCREENCAST -> {
                    activity.castFile(file)
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
        bindDanmu: Boolean,
        options: ActivityOptionsCompat,
        activity: StorageFileActivity
    ) {
        val mediaType = activity.storage.library.mediaType
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

    private fun createShareOptions(
        binding: ItemStorageVideoBinding,
        activity: StorageFileActivity
    ) = ActivityOptionsCompat.makeSceneTransitionAnimation(
        activity,
        Pair(binding.coverIv, binding.coverIv.transitionName),
        Pair(binding.titleTv, binding.titleTv.transitionName)
    )
}