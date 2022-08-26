package com.xyoye.common_component.weight

import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.xyoye.common_component.R
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.databinding.ItemStorageFolderBinding
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.extension.setVideoCover
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.weight.dialog.ExtraSourceDialogUtils
import com.xyoye.data_component.bean.StorageFileBean
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2022/1/19
 */
object StorageAdapter {

    fun newInstance(
        activity: BaseActivity<*, *>,
        mediaType: MediaType,
        refreshDirectory: () -> Unit,
        openFile: (StorageFileBean) -> Unit,
        openDirectory: (StorageFileBean) -> Unit,
        moreAction: ((StorageFileBean) -> Boolean)? = null
    ): BaseAdapter {
        return buildAdapter {
            addItem<StorageFileBean, ItemStorageVideoBinding>(R.layout.item_storage_video) {
                checkType { data, _ -> data is StorageFileBean && data.isDirectory.not() }
                initView { data, _, _ ->
                    itemBinding.coverIv.setVideoCover(data.uniqueKey, data.fileCoverUrl)
                    itemBinding.titleTv.text = data.fileName
                    itemBinding.titleTv.setTextColor(getTitleColor(data.isLastPlay))
                    itemBinding.durationTv.text = getProgress(data.position, data.duration)
                    itemBinding.durationTv.isVisible = data.duration > 0
                    itemBinding.danmuTipsTv.isGone = data.danmuPath.isNullOrEmpty()
                    itemBinding.subtitleTipsTv.isGone = data.subtitlePath.isNullOrEmpty()
                    itemBinding.lastPlayTimeTv.isVisible = data.lastPlayTime != null
                    data.lastPlayTime?.let {
                        itemBinding.lastPlayTimeTv.text = PlayHistoryUtils.formatPlayTime(it)
                    }

                    itemBinding.mainActionFl.setOnClickListener {
                        openFile.invoke(data)
                    }
                    itemBinding.moreActionIv.setOnClickListener {
                        if (moreAction?.invoke(data) == true) {
                            return@setOnClickListener
                        }

                        // TODO: 共享元素的动画有问题，动画总是从最后一个Item开始
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            Pair(itemBinding.coverIv, itemBinding.coverIv.transitionName),
                            Pair(itemBinding.titleTv, itemBinding.titleTv.transitionName)
                        )
                        showVideoManagerDialog(activity, mediaType, data, options, refreshDirectory)
                    }
                    itemBinding.itemLayout.setOnLongClickListener {
                        if (moreAction?.invoke(data) == true) {
                            return@setOnLongClickListener true
                        }

                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            Pair(itemBinding.coverIv, itemBinding.coverIv.transitionName),
                            Pair(itemBinding.titleTv, itemBinding.titleTv.transitionName)
                        )
                        showVideoManagerDialog(activity, mediaType, data, options, refreshDirectory)
                    }
                }
            }

            addItem<StorageFileBean, ItemStorageFolderBinding>(R.layout.item_storage_folder) {
                checkType { data, _ -> data is StorageFileBean && data.isDirectory }
                initView { data, _, _ ->
                    val fileCount = if (data.childFileCount > 0)
                        "${data.childFileCount}文件"
                    else
                        "目录"
                    itemBinding.folderTv.text = data.fileName
                    itemBinding.folderTv.setTextColor(getTitleColor(data.isLastPlay))
                    itemBinding.fileCountTv.text = fileCount
                    itemBinding.itemLayout.setOnClickListener {
                        openDirectory.invoke(data)
                    }
                }
            }
        }
    }

    private fun getTitleColor(isLastPlay: Boolean): Int {
        return if (isLastPlay)
            R.color.text_theme.toResColor()
        else
            R.color.text_black.toResColor()
    }

    private fun getProgress(position: Long, duration: Long): String {
        return if (position > 0 && duration > 0) {
            "${formatDuration(position)}/${formatDuration(duration)}"
        } else if (duration > 0) {
            formatDuration(duration)
        } else {
            ""
        }
    }

    private fun showVideoManagerDialog(
        Activity: BaseActivity<*, *>,
        mediaType: MediaType,
        data: StorageFileBean,
        options: ActivityOptionsCompat,
        refreshDirectory: () -> Unit
    ): Boolean {
        return ExtraSourceDialogUtils.show(
            Activity,
            mediaType,
            data,
            options,
            refreshDirectory
        )
    }
}