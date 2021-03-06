package com.xyoye.common_component.utils.view

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * 路径分割线（以图片实现）
 */
class FilePathItemDecoration(divider: Drawable, dividerSize: Int) : RecyclerView.ItemDecoration() {
    private val mDivider = divider
    private val mDividerSize = dividerSize

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.save()

        //居中显示
        val top = (parent.height - mDividerSize) / 2
        val bottom = top + mDividerSize

        val mBounds = Rect()

        //只在中间绘制
        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)

            val right = mBounds.right + child.translationX.roundToInt()
            val left = right - mDividerSize
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(0, 0, mDividerSize, 0)
    }
}