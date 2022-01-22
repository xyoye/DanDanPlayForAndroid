package com.xyoye.common_component.weight

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.xyoye.common_component.R
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.databinding.ItemStorageFolderBinding
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.extension.setVideoCover
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.weight.dialog.UnBindSourceDialogUtils
import com.xyoye.data_component.bean.StorageFileBean
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2022/1/19
 */
object StorageAdapter {

    fun newInstance(
        activity: AppCompatActivity,
        mediaType: MediaType,
        refreshDirectory: () -> Unit,
        openFile: (String) -> Unit,
        openDirectory: (String) -> Unit
    ): BaseAdapter {
        return buildAdapter {
            addItem<StorageFileBean, ItemStorageVideoBinding>(R.layout.item_storage_video) {
                checkType { data, _ -> data.isDirectory.not() }
                initView { data, _, _ ->


                    itemBinding.coverIv.setVideoCover(data.uniqueKey, data.fileCoverUrl)
                    itemBinding.titleTv.text = data.fileName
                    itemBinding.durationTv.text = getProgress(data.position, data.duration)
                    itemBinding.durationTv.isVisible = data.duration > 0
                    itemBinding.danmuTipsTv.isGone = data.danmuPath.isNullOrEmpty()
                    itemBinding.subtitleTipsTv.isGone = data.subtitlePath.isNullOrEmpty()
                    itemBinding.moreActionIv.isGone =
                        data.danmuPath.isNullOrEmpty() && data.subtitlePath.isNullOrEmpty()

                    itemBinding.itemLayout.setOnClickListener {
                        openFile.invoke(data.uniqueKey ?: "")
                    }
                    itemBinding.moreActionIv.setOnClickListener {
                        showVideoManagerDialog(activity, mediaType, data, refreshDirectory)
                    }
                    itemBinding.itemLayout.setOnLongClickListener {
                        showVideoManagerDialog(activity, mediaType, data, refreshDirectory)
                    }
                }
            }

            addItem<StorageFileBean, ItemStorageFolderBinding>(R.layout.item_storage_folder) {
                checkType { data, _ -> data.isDirectory }
                initView { data, _, _ ->
                    val fileCount = if (data.childFileCount > 0)
                        "${data.childFileCount}文件"
                    else
                        "目录"
                    itemBinding.folderTv.text = data.fileName
                    itemBinding.fileCountTv.text = fileCount
                    itemBinding.itemLayout.setOnClickListener {
                        openDirectory.invoke(data.filePath)
                    }
                }
            }
        }
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
        Activity: AppCompatActivity,
        mediaType: MediaType,
        bean: StorageFileBean,
        refreshDirectory: () -> Unit
    ): Boolean {
        return UnBindSourceDialogUtils.show(
            Activity,
            mediaType,
            bean.uniqueKey,
            bean.danmuPath,
            bean.subtitlePath,
            refreshDirectory
        )
    }
}