package com.xyoye.stream_component.ui.fragment.storage_file

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextUtils
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.xyoye.stream_component.R
import com.xyoye.stream_component.ui.activities.storage_file.StorageFileActivity

/**
 * Created by xyoye on 2023/4/13
 */

class StorageFileAdapter(
    private val activity: StorageFileActivity,
    private val viewModel: StorageFileFragmentViewModel
) {

    private enum class ManageAction(val title: String, val icon: Int) {
        SCREENCAST("投屏", com.xyoye.common_component.R.drawable.ic_video_cast),
        BIND_DANMU("手动查找弹幕", com.xyoye.common_component.R.drawable.ic_bind_danmu_manual),
        BIND_SUBTITLE("手动查找字幕", com.xyoye.common_component.R.drawable.ic_bind_subtitle),
        UNBIND_DANMU("移除弹幕绑定", com.xyoye.common_component.R.drawable.ic_unbind_danmu),
        UNBIND_SUBTITLE("移除字幕绑定", com.xyoye.common_component.R.drawable.ic_unbind_subtitle);

        fun toAction() = SheetActionBean(this, title, icon)
    }

    private val tagDecoration = ItemDecorationOrientation(5.dp(), 0, RecyclerView.HORIZONTAL)

    fun create(): BaseAdapter {
        return buildAdapter {
            setupVerticalAnimation()

            setupDiffUtil {
                newDataInstance { it }
                areItemsTheSame(isSameStorageFileItem())
                areContentsTheSame(isSameStorageFileContent())
            }

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

    private fun isSameStorageFileItem() = { old: Any, new: Any ->
        (old as? StorageFile)?.uniqueKey() == (new as? StorageFile)?.uniqueKey()
    }

    private fun isSameStorageFileContent() = { old: Any, new: Any ->
        val oldItem = old as? StorageFile?
        val newItem = new as? StorageFile?
        oldItem?.fileUrl() == newItem?.fileUrl()
                && oldItem?.fileName() == newItem?.fileName()
                && oldItem?.childFileCount() == newItem?.childFileCount()
                && oldItem?.playHistory == newItem?.playHistory
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
                activity.openDirectory(data)
            }
        }

    private fun BaseViewHolderCreator<ItemStorageVideoBinding>.videoItem() = { data: StorageFile ->
        itemBinding.run {
            coverIv.loadImage(data)

            titleTv.text = data.fileName()
            titleTv.setTextColor(getTitleColor(data))

            val duration = getDuration(data)
            durationTv.text = duration
            durationTv.isVisible = duration.isNotEmpty()

            setupVideoTag(tagRv, data)

            mainActionFl.setOnClickListener {
                activity.openFile(data)
            }

            moreActionIv.setOnClickListener {
                showMoreAction(data, createShareOptions(itemLayout))
            }

            mainActionFl.setOnLongClickListener {
                showMoreAction(data, createShareOptions(itemLayout))
                return@setOnLongClickListener true
            }
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
        var duration = file.playHistory?.videoDuration ?: 0
        if (duration == 0L) {
            duration = file.videoDuration()
        }
        return if (position > 0 && duration > 0) {
            "${formatDuration(position)}/${formatDuration(duration)}"
        } else if (duration > 0) {
            formatDuration(duration)
        } else {
            ""
        }
    }

    private fun getPlayTime(file: StorageFile): String {
        // Url为空，意味着该历史记录为资源绑定记录，非播放记录
        if (TextUtils.isEmpty(file.playHistory?.url)) {
            return ""
        }
        return file.playHistory?.playTime?.run {
            PlayHistoryUtils.formatPlayTime(this)
        } ?: ""
    }

    private fun isShowDanmu(file: StorageFile): Boolean {
        return file.playHistory?.danmuPath?.isNotEmpty() == true
    }

    private fun isShowSubtitle(file: StorageFile): Boolean {
        return file.playHistory?.subtitlePath?.isNotEmpty() == true
    }

    private fun showMoreAction(file: StorageFile, options: ActivityOptionsCompat) {
        BottomActionDialog(activity, getMoreActions(file)) {
            when (it.actionId) {
                ManageAction.BIND_DANMU -> bindExtraSource(file, true, options)
                ManageAction.BIND_SUBTITLE -> bindExtraSource(file, false, options)
                ManageAction.UNBIND_DANMU -> viewModel.unbindExtraSource(file, true)
                ManageAction.UNBIND_SUBTITLE -> viewModel.unbindExtraSource(file, false)
                ManageAction.SCREENCAST -> activity.castFile(file)
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
        options: ActivityOptionsCompat
    ) {
        activity.shareStorageFile = file
        ARouter.getInstance()
            .build(RouteTable.Local.BindExtraSource)
            .withBoolean("isSearchDanmu", bindDanmu)
            .withOptionsCompat(options)
            .navigation(activity)
    }

    private fun createShareOptions(
        itemLayout: ConstraintLayout,
    ) = ActivityOptionsCompat.makeSceneTransitionAnimation(
        activity,
        Pair(itemLayout, itemLayout.transitionName),
    )
}