package com.xyoye.stream_component.utils.storage

import androidx.fragment.app.Fragment
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.toResColor
import com.xyoye.data_component.bean.StorageFilePath
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ItemStoragePathBinding
import com.xyoye.stream_component.databinding.ItemStoragePathDividerBinding

/**
 * Created by xyoye on 2023/1/1.
 */

object StorageFilePathAdapter {
    object PathDivider
    object MarginDivider

    private val marginDivider= MarginDivider
    private val pathDivider = PathDivider

    fun build(onPathClick: (path: StorageFilePath) -> Unit) = buildAdapter {
        addItem<StorageFilePath, ItemStoragePathBinding>(R.layout.item_storage_path) {
            checkType { data -> data is StorageFilePath }

            initView { data ->
                itemBinding.tvPath.text = data.name
                itemBinding.tvPath.setTextColor(getPathColor(data))
                itemBinding.tvPath.setOnClickListener {
                    onPathClick.invoke(data)
                }
            }
        }

        addItem<PathDivider, ItemStoragePathDividerBinding>(R.layout.item_storage_path_divider) {
            checkType { data -> data is PathDivider }
        }

        addItem<MarginDivider, ItemStoragePathDividerBinding>(R.layout.item_storage_path_margin) {
            checkType { data -> data is MarginDivider }
        }
    }

    fun buildPathData(routeFragments: MutableMap<StorageFilePath, Fragment>): List<Any> {
        val pathList = mutableListOf<Any>(marginDivider)
        val lastIndex = routeFragments.keys.size - 1
        routeFragments.keys.forEachIndexed { index, path ->
            val lastItem = index == lastIndex
            path.isLast = lastItem
            pathList.add(path)

            if (lastItem.not()) {
                pathList.add(pathDivider)
            }
        }
        return pathList
    }

    private fun getPathColor(path: StorageFilePath): Int {
        return if (path.isLast) {
            R.color.text_theme
        } else {
            R.color.text_black
        }.toResColor()
    }
}