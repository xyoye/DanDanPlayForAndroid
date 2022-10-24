package com.xyoye.common_component.utils.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * 不同方向上的分割线
 *
 * Created by xyoye on 2022/10/13
 */

class ItemDecorationOrientation : ItemDecoration {
    private val dividerPx: Int
    private val headerPx: Int
    private val footerPx: Int
    private val orientation: Int

    constructor(dividerPx: Int, @RecyclerView.Orientation orientation: Int) : this(
        dividerPx,
        dividerPx,
        orientation
    )

    constructor(
        dividerPx: Int,
        headerFooterPx: Int,
        @RecyclerView.Orientation orientation: Int
    ) : this(dividerPx, headerFooterPx, headerFooterPx, orientation)

    constructor(
        dividerPx: Int,
        headerPx: Int,
        footerPx: Int,
        @RecyclerView.Orientation orientation: Int
    ) {
        this.dividerPx = dividerPx
        this.headerPx = headerPx
        this.footerPx = footerPx
        this.orientation = orientation
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (orientation == RecyclerView.VERTICAL) {
            getItemOffsetsVertical(outRect, view, parent)
        } else {
            getItemOffsetsHorizontal(outRect, view, parent)
        }
    }

    private fun getItemOffsetsVertical(outRect: Rect, view: View, parent: RecyclerView) {
        val itemCount = parent.adapter?.itemCount ?: return
        val position = parent.getChildAdapterPosition(view)

        if (position == 0) {
            outRect.top = headerPx
        } else {
            outRect.top = position * dividerPx / itemCount
        }

        if (position == itemCount - 1) {
            outRect.bottom = footerPx
        } else {
            outRect.bottom = dividerPx - (position + 1) * dividerPx / itemCount
        }
    }

    private fun getItemOffsetsHorizontal(outRect: Rect, view: View, parent: RecyclerView) {
        val itemCount = parent.adapter?.itemCount ?: return
        val position = parent.getChildAdapterPosition(view)

        if (position == 0) {
            outRect.left = headerPx
        } else {
            outRect.left = position * dividerPx / itemCount
        }

        if (position == itemCount - 1) {
            outRect.right = footerPx
        } else {
            outRect.right = dividerPx - (position + 1) * dividerPx / itemCount
        }
    }
}