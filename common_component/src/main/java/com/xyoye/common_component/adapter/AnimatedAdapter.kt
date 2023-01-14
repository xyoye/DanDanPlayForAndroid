package com.xyoye.common_component.adapter

import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.R

/**
 * Created by xyoye on 2023/1/2.
 */

abstract class AnimatedAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    companion object {
        private const val ANIMATION_STAGGER_MILLIS = 20
    }

    private var isAnimating = false

    private var animationStartOffset = 0

    private val stopAnimationHandler = Handler(Looper.getMainLooper())
    private val stopAnimationRunnable = Runnable { stopAnimation() }


    private var recyclerView: RecyclerView? = null
    private val clearAnimationListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            clearAnimation()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(clearAnimationListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        recyclerView.removeOnScrollListener(clearAnimationListener)
        this.recyclerView = null
    }

    protected fun bindViewHolderAnimation(holder: VH) {
        holder.itemView.clearAnimation()
        if (isAnimating) {
            val animation = AnimationUtils.loadAnimation(
                holder.itemView.context, R.anim.recycler_view_item
            ).apply { startOffset = animationStartOffset.toLong() }
            animationStartOffset += ANIMATION_STAGGER_MILLIS
            holder.itemView.startAnimation(animation)
            postStopAnimation()
        }
    }

    private fun stopAnimation() {
        stopAnimationHandler.removeCallbacks(stopAnimationRunnable)
        isAnimating = false
        animationStartOffset = 0
    }

    private fun postStopAnimation() {
        stopAnimationHandler.removeCallbacks(stopAnimationRunnable)
        stopAnimationHandler.post(stopAnimationRunnable)
    }

    private fun clearAnimation() {
        stopAnimation()
        recyclerView?.let {
            for (index in 0 until it.childCount) {
                it.getChildAt(index).clearAnimation()
            }
        }
    }

    open fun resetAnimation() {
        clearAnimation()
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
        isAnimating = layoutManager?.orientation == RecyclerView.VERTICAL
    }

    @CallSuper
    open fun setData(data: List<Any>) {
        resetAnimation()
    }
}