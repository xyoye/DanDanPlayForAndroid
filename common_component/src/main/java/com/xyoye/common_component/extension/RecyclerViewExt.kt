package com.xyoye.common_component.extension

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.R
import com.xyoye.common_component.adapter.BaseAdapter

/**
 * Created by xyoye on 2020/8/17.
 */

fun RecyclerView.vertical(reverse: Boolean = false): LinearLayoutManager {
    return LinearLayoutManager(context, LinearLayoutManager.VERTICAL, reverse)
}

fun RecyclerView.horizontal(reverse: Boolean = false): LinearLayoutManager {
    return LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, reverse)
}

fun RecyclerView.grid(spanCount: Int): GridLayoutManager {
    return GridLayoutManager(context, spanCount)
}

fun RecyclerView.gridEmpty(spanCount: Int): GridLayoutManager {
    return GridLayoutManager(context, spanCount).also {
        it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position == RecyclerView.NO_POSITION) {
                    return 1
                }
                val viewType = adapter?.getItemViewType(position)
                if (viewType != BaseAdapter.VIEW_TYPE_EMPTY) {
                    return 1
                }
                return spanCount
            }
        }
    }
}

fun RecyclerView.setData(items: List<Any>) {
    (adapter as? BaseAdapter)?.setData(items)
}

fun RecyclerView.requestIndexChildFocus(index: Int): Boolean {
    scrollToPosition(index)

    val targetTag = R.string.focusable_item.toResString()
    val indexView = layoutManager?.findViewByPosition(index)
    if (indexView != null) {
        indexView.findViewWithTag<View>(targetTag)?.requestFocus()
        return true
    }

    post {
        layoutManager?.findViewByPosition(index)
            ?.findViewWithTag<View>(targetTag)
            ?.requestFocus()
    }
    return true
}