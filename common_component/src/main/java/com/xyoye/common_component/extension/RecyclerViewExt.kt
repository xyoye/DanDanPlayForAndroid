package com.xyoye.common_component.extension

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.R
import com.xyoye.common_component.adapter.BaseAdapter

/**
 * Created by xyoye on 2020/8/17.
 */

fun RecyclerView.vertical(reverse: Boolean = false): LinearLayoutManager {
    return LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
}

fun RecyclerView.horizontal(reverse: Boolean = false): LinearLayoutManager {
    return LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
}

fun RecyclerView.grid(spanCount: Int): GridLayoutManager {
    return GridLayoutManager(context, spanCount)
}

fun RecyclerView.gridEmpty(spanCount: Int): GridLayoutManager {
    return GridLayoutManager(context, spanCount).also {
        it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (adapter is RecyclerView.Adapter<RecyclerView.ViewHolder>) {
                    if ((adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)
                            .getItemViewType(position) == BaseAdapter.VIEW_TYPE_EMPTY
                    ) return spanCount
                }
                return 1
            }
        }
    }
}

fun RecyclerView.setData(items: List<Any>) {
    adapter?.apply {
        if (this is BaseAdapter) {
            this.setData(items)
        }
    }
}

inline fun <reified T> RecyclerView.getChildViewBindingAt(index: Int): T? {
    val binding = getChildAt(index)?.run {
        DataBindingUtil.getBinding<ViewDataBinding>(this)
    } ?: return null

    return if (binding is T) {
        binding
    } else {
        null
    }
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