package com.xyoye.local_component.ui.activities.play_history

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.adapter.*
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.databinding.ItemStorageVideoTagBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.FastClickFilter
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.bean.VideoTagBean
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.R
import java.io.File

/**
 * Created by xyoye on 2023/4/14
 */

class PlayHistoryAdapter(
    private val activity: PlayHistoryActivity,
    private val viewModel: PlayHistoryViewModel
) {

    private enum class EditHistory(val title: String, val icon: Int) {
        REMOVE_DANMU("移除弹幕绑定", R.drawable.ic_unbind_danmu),
        REMOVE_SUBTITLE("移除字幕绑定", R.drawable.ic_unbind_subtitle),
        COPY_URL("复制播放链接", R.drawable.ic_copy_url),
        DELETE_HISTORY("删除播放记录", R.drawable.ic_delete_history);

        fun toAction() = SheetActionBean(this, title, icon)
    }

    private val tagDecoration = ItemDecorationOrientation(5.dp(), 0, RecyclerView.HORIZONTAL)

    fun createAdapter(): BaseAdapter {
        return buildAdapter {
            addEmptyView(R.layout.layout_empty) {
                initEmptyView {
                    itemBinding.emptyTv.text = "暂无播放记录"
                }
            }

            addItem(R.layout.item_storage_video) {
                initView(historyItem(activity, viewModel))
            }
        }
    }

    private fun BaseViewHolderCreator<ItemStorageVideoBinding>.historyItem(
        activity: PlayHistoryActivity,
        viewModel: PlayHistoryViewModel
    ) =
        { data: PlayHistoryEntity ->
            itemBinding.coverIv.loadVideoCover(data.uniqueKey.toCoverFile())

            itemBinding.durationTv.text = getDuration(data)
            itemBinding.durationTv.isVisible = data.videoDuration > 0

            val isInvalid = isHistoryInvalid(data)
            val titleTextColor = if (isInvalid)
                R.color.text_gray
            else
                R.color.text_black

            itemBinding.titleTv.setTextColor(titleTextColor.toResColor())
            itemBinding.titleTv.text = data.videoName

            setupVideoTag(itemBinding.tagRv, data)

            itemBinding.itemLayout.setOnClickListener {
                //防止快速点击
                if (FastClickFilter.isNeedFilter())
                    return@setOnClickListener

                if (isInvalid) {
                    ToastCenter.showError("记录已失效，无法播放")
                    return@setOnClickListener
                }
                viewModel.openHistory(data)
            }

            itemBinding.moreActionIv.setOnClickListener {
                showEditDialog(data, activity, viewModel)
            }
            itemBinding.itemLayout.setOnLongClickListener {
                showEditDialog(data, activity, viewModel)
                return@setOnLongClickListener true
            }
        }

    private fun setupVideoTag(tagRv: RecyclerView, data: PlayHistoryEntity) {
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

    private fun generateVideoTags(data: PlayHistoryEntity): List<VideoTagBean> {
        val tagList = mutableListOf<VideoTagBean>()
        if (data.danmuPath?.isNotEmpty() == true) {
            tagList.add(VideoTagBean("弹幕", R.color.theme.toResColor()))
        }
        if (data.subtitlePath?.isNotEmpty() == true) {
            tagList.add(VideoTagBean("字幕", R.color.orange.toResColor()))
        }
        val progress = getProgress(data)
        if (progress.isNotEmpty()) {
            tagList.add(VideoTagBean(progress, R.color.gray_40.toResColor()))
        }
        tagList.add(VideoTagBean(data.mediaType.storageName, R.color.gray_40.toResColor()))
        tagList.add(VideoTagBean(PlayHistoryUtils.formatPlayTime(data.playTime), R.color.gray_40.toResColor()))
        return tagList
    }

    private fun isHistoryInvalid(entity: PlayHistoryEntity): Boolean {
        return when (entity.mediaType) {
            MediaType.MAGNET_LINK -> {
                val torrentPath = entity.torrentPath
                //磁链种子文件丢失
                if (torrentPath.isNullOrEmpty() || entity.torrentIndex == -1) {
                    return true
                }
                val torrentFile = File(torrentPath)
                return !torrentFile.exists()
            }
            else -> entity.storageId == null
        }
    }

    private fun getProgress(data: PlayHistoryEntity): String {
        val position = data.videoPosition
        val duration = data.videoDuration
        if (position == 0L || duration == 0L) {
            return ""
        }

        var progress = (position * 100f / duration).toInt()
        if (progress == 0) {
            progress = 1
        }
        return "进度 $progress%"
    }

    private fun getDuration(data: PlayHistoryEntity): String {
        val position = data.videoPosition
        val duration = data.videoDuration
        return if (position > 0 && duration > 0) {
            "${formatDuration(position)}/${formatDuration(duration)}"
        } else if (duration > 0) {
            formatDuration(duration)
        } else {
            ""
        }
    }

    private fun showEditDialog(
        history: PlayHistoryEntity,
        activity: PlayHistoryActivity,
        viewModel: PlayHistoryViewModel
    ) {
        val actions = mutableListOf<SheetActionBean>()
        if (history.danmuPath.isNullOrEmpty().not()) {
            actions.add(EditHistory.REMOVE_DANMU.toAction())
        }
        if (history.subtitlePath.isNullOrEmpty().not()) {
            actions.add(EditHistory.REMOVE_SUBTITLE.toAction())
        }
        actions.add(EditHistory.COPY_URL.toAction())
        actions.add(EditHistory.DELETE_HISTORY.toAction())
        BottomActionDialog(activity, actions) {
            when (it.actionId) {
                EditHistory.REMOVE_DANMU -> viewModel.unbindDanmu(history)
                EditHistory.REMOVE_SUBTITLE -> viewModel.unbindSubtitle(history)
                EditHistory.DELETE_HISTORY -> viewModel.removeHistory(history)
                EditHistory.COPY_URL -> {
                    history.url.addToClipboard()
                    ToastCenter.showSuccess("链接已复制！")
                }
            }
            return@BottomActionDialog true
        }.show()
    }
}