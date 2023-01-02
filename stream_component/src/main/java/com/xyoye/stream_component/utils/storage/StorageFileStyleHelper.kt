package com.xyoye.stream_component.utils.storage

import android.animation.ValueAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.extension.isNightMode
import com.xyoye.common_component.extension.toResColor
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityStorageFileBinding
import com.xyoye.stream_component.ui.activities.storage_file.StorageFileActivity
import com.xyoye.stream_component.ui.weight.StorageFileBehavior

/**
 * Created by xyoye on 2023/1/1.
 */

class StorageFileStyleHelper(
    private val activity: StorageFileActivity,
    private val binding: ActivityStorageFileBinding
) {
    //标题栏是否已折叠
    private var mToolbarCollapsed: Boolean = false

    //标题栏折叠后颜色
    private val mToolbarCollapsedColor = R.color.layout_bg_color.toResColor()

    //标题栏展开后颜色
    private val mToolbarExpandedColor = R.color.item_bg_color.toResColor()

    //标题栏颜色动画
    private var mColorAnimator: ValueAnimator? = null

    init {
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mColorAnimator?.cancel()
                }
            }
        })
    }

    /**
     * 监听子View滚动以改变状态栏与标题栏颜色
     */
    fun observerChildScroll() {
        val view = binding.fragmentContainer
        val layoutParams = view.layoutParams as? CoordinatorLayout.LayoutParams
        val behavior = layoutParams?.behavior as? StorageFileBehavior
        behavior?.observerToolbarCollapsed {
            changeToolbarStyle(it)
        }
    }

    private fun changeToolbarStyle(collapsed: Boolean) {
        if (mToolbarCollapsed == collapsed) {
            return
        }
        mToolbarCollapsed = collapsed
        startAnimation()
    }

    private fun startAnimation() {
        mColorAnimator?.cancel()

        ValueAnimator.ofArgb(
            getAnimatorFromColor(), getAnimatorToColor()
        ).apply {
            duration = 300
            addUpdateListener { changeStyle(it.animatedValue as Int) }
            start()
        }.also {
            mColorAnimator = it
        }
    }

    private fun changeStyle(color: Int) {
        ImmersionBar.with(activity)
            .statusBarColorInt(color)
            .fitsSystemWindows(true)
            .statusBarDarkFont(!activity.isNightMode())
            .init()
        binding.appbarLayout.setBackgroundColor(color)
    }

    private fun getAnimatorFromColor(): Int {
        return if (mToolbarCollapsed) {
            mToolbarExpandedColor
        } else {
            mToolbarCollapsedColor
        }
    }

    private fun getAnimatorToColor(): Int {
        return if (mToolbarCollapsed) {
            mToolbarCollapsedColor
        } else {
            mToolbarExpandedColor
        }
    }
}