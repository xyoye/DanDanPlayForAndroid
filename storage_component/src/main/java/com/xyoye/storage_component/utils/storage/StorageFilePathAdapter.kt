package com.xyoye.storage_component.utils.storage

import android.view.KeyEvent
import android.view.View
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.setupDiffUtil
import com.xyoye.common_component.extension.toResColor
import com.xyoye.data_component.bean.StorageFilePath
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.ItemStoragePathBinding
import com.xyoye.storage_component.databinding.ItemStoragePathDividerBinding
import com.xyoye.storage_component.ui.activities.storage_file.StorageFileActivity
import com.xyoye.storage_component.ui.fragment.storage_file.StorageFileFragment

/**
 * Created by xyoye on 2023/1/1.
 */

object StorageFilePathAdapter {
    object PathDivider
    object MarginDivider

    private val marginDivider = MarginDivider
    private val pathDivider = PathDivider

    fun build(activity: StorageFileActivity, onPathClick: (path: StorageFilePath) -> Unit) = buildAdapter {

        setupDiffUtil {
            areContentsTheSame(isSameStoragePathContent())
        }

        addItem<StorageFilePath, ItemStoragePathBinding>(R.layout.item_storage_path) {
            checkType { data -> data is StorageFilePath }

            initView { data ->
                itemBinding.tvPath.text = data.name
                itemBinding.tvPath.setTextColor(getPathColor(data))
                itemBinding.tvPath.setOnClickListener {
                    onPathClick.invoke(data)
                }

                itemBinding.tvPath.setOnKeyListener(object : View.OnKeyListener {
                    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                        if (event?.action != KeyEvent.ACTION_DOWN || v?.isFocused != true) {
                            return false
                        }
                        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            //由于系统无法将焦点传递到Fragment，手动处理焦点传递
                            activity.dispatchFocus()
                            return true
                        }
                        return false
                    }
                })
            }
        }

        addItem<PathDivider, ItemStoragePathDividerBinding>(R.layout.item_storage_path_divider) {
            checkType { data -> data is PathDivider }
        }

        addItem<MarginDivider, ItemStoragePathDividerBinding>(R.layout.item_storage_path_margin) {
            checkType { data -> data is MarginDivider }
        }
    }

    fun buildPathData(routeFragments: MutableMap<StorageFilePath, StorageFileFragment>): List<Any> {
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

    private fun isSameStoragePathContent() = { old: Any, new: Any ->
        val oldItem = old as? StorageFilePath?
        val newItem = new as? StorageFilePath?
        oldItem?.name == newItem?.name
                && oldItem?.route == newItem?.route
                && oldItem?.isLast == newItem?.isLast
    }

    private fun getPathColor(path: StorageFilePath): Int {
        return if (path.isLast) {
            R.color.text_theme
        } else {
            R.color.text_black
        }.toResColor()
    }
}