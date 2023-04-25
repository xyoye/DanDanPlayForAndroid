package com.xyoye.storage_component.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

/**
 * Created by xyoye on 2023/1/1.
 */

class StorageFileBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    private var onToolbarCollapsedChanged: ((Boolean) -> Unit)? = null

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return true
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        enablingRecyclerViewOverScroll(consumed)

        val isToolbarCollapsed = dyConsumed > 0 || dyUnconsumed > 0
        val isChildScrollExpand = dyUnconsumed < 0
        if (isToolbarCollapsed) {
            onToolbarCollapsedChanged?.invoke(true)
        } else if (isChildScrollExpand) {
            onToolbarCollapsedChanged?.invoke(false)
        }
    }

    /**
     * 设置垂直方向上消耗的距离为0，以使RecyclerView OverScroll动画正常生效
     *
     * 在RecyclerView的scrollByInternal方法中，通过获取垂直方向上滑动时未消耗距离来实现OverScroll
     * 但执行至Behavior中的onNestedScroll方法时，默认会设置消耗所有滑动距离
     * 因此通过重写onNestedScroll，设置不消耗，以使OverScroll动画生效
     * @see RecyclerView.scrollByInternal
     * @see CoordinatorLayout.Behavior.onNestedScroll
     */
    private fun enablingRecyclerViewOverScroll(consumed: IntArray) {
        consumed[1] = 0
    }

    fun observerToolbarCollapsed(block: (Boolean) -> Unit) {
        onToolbarCollapsedChanged = block
    }
}