package com.xyoye.local_component.ui.activities.bind_source

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.adapter.BaseViewHolderCreator
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.databinding.ItemStorageVideoTagBinding
import com.xyoye.common_component.extension.dp
import com.xyoye.common_component.extension.horizontal
import com.xyoye.common_component.extension.loadStorageFileCover
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.extension.toResDrawable
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.data_component.bean.VideoTagBean
import com.xyoye.local_component.databinding.ActivityBindExtraSourceBinding

/**
 * Created by xyoye on 2023/4/14
 */

object VideoItemLayout {

    private val tagDecoration = ItemDecorationOrientation(5.dp(), 0, RecyclerView.HORIZONTAL)

    fun initVideoLayout(dataBinding: ActivityBindExtraSourceBinding, data: StorageFile) {
        dataBinding.videoLayout.run {
            itemLayout.setBackgroundColor(com.xyoye.common_component.R.color.item_bg_color.toResColor())

            titleTv.setTextIsSelectable(true)

            coverIv.loadStorageFileCover(data)

            titleTv.text = data.fileName()

            val duration = getDuration(data)
            durationTv.text = duration
            durationTv.isVisible = duration.isNotEmpty()

            setupVideoTag(tagRv, data)

            dividerView.isVisible = false
        }
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

    private fun setupVideoTag(tagRv: RecyclerView, data: StorageFile) {
        tagRv.apply {
            layoutManager = horizontal()
            adapter = buildAdapter {
                addItem(com.xyoye.common_component.R.layout.item_storage_video_tag) {
                    initView(tagItem())
                }
            }
            removeItemDecoration(tagDecoration)
            addItemDecoration(tagDecoration)
            setData(generateVideoTags(data))
        }
    }

    private fun BaseViewHolderCreator<ItemStorageVideoTagBinding>.tagItem() =
        { data: VideoTagBean ->
            val background = com.xyoye.common_component.R.drawable.background_video_tag.toResDrawable()
            background?.colorFilter = PorterDuffColorFilter(data.color, PorterDuff.Mode.SRC)
            itemBinding.textView.background = background
            itemBinding.textView.text = data.tag
        }

    private fun generateVideoTags(data: StorageFile): List<VideoTagBean> {
        val tagList = mutableListOf<VideoTagBean>()
        if (isShowDanmu(data)) {
            tagList.add(VideoTagBean("弹幕", com.xyoye.common_component.R.color.theme.toResColor()))
        }
        if (isShowSubtitle(data)) {
            tagList.add(VideoTagBean("字幕", com.xyoye.common_component.R.color.orange.toResColor()))
        }
        val progress = getProgress(data)
        if (progress.isNotEmpty()) {
            tagList.add(VideoTagBean(progress, com.xyoye.common_component.R.color.black_alpha.toResColor()))
        }
        val lastPlayTime = getPlayTime(data)
        if (lastPlayTime.isNotEmpty()) {
            tagList.add(VideoTagBean(lastPlayTime, com.xyoye.common_component.R.color.black_alpha.toResColor()))
        }
        return tagList
    }

    private fun isShowDanmu(file: StorageFile): Boolean {
        return file.playHistory?.danmuPath?.isNotEmpty() == true
    }

    private fun isShowSubtitle(file: StorageFile): Boolean {
        return file.playHistory?.subtitlePath?.isNotEmpty() == true
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

    private fun getPlayTime(file: StorageFile): String {
        // Url为空，意味着该历史记录为资源绑定记录，非播放记录
        if (TextUtils.isEmpty(file.playHistory?.url)) {
            return ""
        }
        return file.playHistory?.playTime?.run {
            PlayHistoryUtils.formatPlayTime(this)
        } ?: ""
    }
}