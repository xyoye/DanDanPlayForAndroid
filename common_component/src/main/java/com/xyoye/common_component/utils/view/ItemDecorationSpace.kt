package com.xyoye.common_component.utils.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * 空白的分割线
 *
 * Created by xyoye on 2019/3/26.
 */
class ItemDecorationSpace : ItemDecoration {
    private var top: Int
    private var left: Int
    private var right: Int
    private var bottom: Int
    private var spanCount: Int

    constructor(space: Int) : this(space, space, space, space)

    constructor(spaceLR: Int, spaceTB: Int) : this(spaceTB, spaceLR, spaceLR, spaceTB)

    constructor(top: Int, left: Int, right: Int, bottom: Int) {
        this.top = top
        this.left = left
        this.right = right
        this.bottom = bottom
        spanCount = 0
    }

    constructor(top: Int, left: Int, right: Int, bottom: Int, spanCount: Int) {
        this.top = top
        this.left = left
        this.right = right
        this.bottom = bottom
        this.spanCount = spanCount
    }

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.top = top
        outRect.left = left
        outRect.bottom = bottom
        if (spanCount != 0) {
            val position = parent.getChildLayoutPosition(view)
            if ((position + 1) % spanCount == 0) {
                outRect.right = 0
            } else {
                outRect.right = right
            }
        } else {
            outRect.right = right
        }
    }
}