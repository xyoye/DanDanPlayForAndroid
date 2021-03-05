package com.xyoye.user_component.ui.weight

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.xyoye.user_component.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Created by xyoye on 2021/3/4.
 *
 * copyright https://github.com/cachapa/ExpandableLayout
 */

class ExpandableLayout : FrameLayout {

    enum class State {
        COLLAPSED,
        COLLAPSING,
        EXPANDING,
        EXPANDED
    }

    companion object {
        @JvmStatic
        private var cacheLayout: ExpandableLayout? = null

        private const val DEFAULT_DURATION = 300

        private const val HORIZONTAL = 0
        private const val VERTICAL = 1

        private const val KEY_EXPANSION = "key_expansion"
        private const val KEY_SUPER_STATE = "key_super_state"
    }

    var mDuration = 0
    var mExpansion = 0f
    private var orientation = VERTICAL
    private var parallax = 0f

    private var currentState = State.COLLAPSED
    var mAnimator: ValueAnimator? = null
    var mInterpolator = LinearInterpolator()

    private var mExpansionUpdateBlock: ((Float, State) -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        attrs?.apply {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout)
            mDuration = typedArray.getInt(R.styleable.ExpandableLayout_duration, DEFAULT_DURATION)
            val expanded = typedArray.getBoolean(R.styleable.ExpandableLayout_expanded, false)
            orientation =
                typedArray.getInt(R.styleable.ExpandableLayout_android_orientation, VERTICAL)
            parallax = typedArray.getFloat(R.styleable.ExpandableLayout_parallax, 0f)
            typedArray.recycle()

            mExpansion = if (expanded) 1f else 0f
            currentState = if (expanded) State.EXPANDED else State.COLLAPSED
            setParallax(parallax)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return Bundle().apply {
            mExpansion = if (isExpanded()) 1f else 0f
            putFloat(KEY_EXPANSION, mExpansion)
            putParcelable(KEY_SUPER_STATE, superState)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null && state is Bundle) {
            mExpansion = state.getFloat(KEY_EXPANSION)
            currentState = if (mExpansion == 1f) State.EXPANDED else State.COLLAPSED
            val superSate: Parcelable? = state.getParcelable(KEY_SUPER_STATE)
            super.onRestoreInstanceState(superSate)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val size = if (orientation == LinearLayout.HORIZONTAL) measuredWidth else measuredHeight
        isGone = mExpansion == 0f && size == 0

        val expansionDelta = size - round(size * mExpansion)
        if (parallax > 0) {
            val parallaxDelta = expansionDelta * parallax
            for (index in 0..childCount) {
                val childView = getChildAt(index)
                if (orientation == HORIZONTAL) {
                    val direction = if (layoutDirection == LAYOUT_DIRECTION_RTL) 1 else -1
                    childView.translationX = direction * parallaxDelta
                } else {
                    childView.translationY = -parallaxDelta
                }

            }
        }

        if (orientation == HORIZONTAL) {
            setMeasuredDimension((measuredWidth - expansionDelta).toInt(), measuredHeight)
        } else {
            setMeasuredDimension(measuredWidth, (measuredHeight - expansionDelta).toInt())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        mAnimator?.cancel()
        super.onConfigurationChanged(newConfig)
    }

    fun getCurrentState() = currentState

    fun isExpanded(): Boolean {
        return currentState == State.EXPANDING || currentState == State.EXPANDED
    }

    fun toggle(animate: Boolean = true) {
        cacheLayout = if (isExpanded()) {
            if (cacheLayout != this) {
                collapse(animate)
            }
            cacheLayout?.collapse()
            null
        } else {
            cacheLayout?.collapse()
            expand(animate)
            this
        }
    }

    fun collapse(animate: Boolean = true) {
        setExpanded(false, animate)
    }

    fun setExpansionObserver(block: (expansionFraction: Float, state: State) -> Unit) {
        mExpansionUpdateBlock = block
    }

    fun expand(animate: Boolean = true) {
        setExpanded(true, animate)
    }

    fun setParallax(parallaxValue: Float) {
        var value = max(0f, parallaxValue)
        value = min(1f, value)
        parallax = value
    }

    fun getParallax() = parallax

    private fun setExpanded(expanded: Boolean, animate: Boolean) {
        if (expanded == isExpanded()) {
            return
        }
        val targetExpansion = if (expanded) 1f else 0f
        if (animate) {
            animeSize(targetExpansion)
        } else {
            setExpansion(targetExpansion)
        }
    }

    private fun animeSize(targetExpansion: Float) {
        mAnimator?.cancel()

        mAnimator = ValueAnimator.ofFloat(mExpansion, targetExpansion).apply {
            interpolator = mInterpolator
            duration = mDuration.toLong()
            addUpdateListener {
                setExpansion(it.animatedValue as Float)
            }
            addListener(ExpansionListener(targetExpansion))
            start()
        }
    }

    private fun setExpansion(expansion: Float) {
        if (expansion == mExpansion) {
            return
        }

        val delta = expansion - mExpansion
        currentState = when {
            delta == 0f -> State.COLLAPSED
            delta == 1f -> State.EXPANDED
            delta < 0 -> State.COLLAPSING
            delta > 0 -> State.EXPANDING
            else -> State.COLLAPSED
        }

        isGone = currentState == State.COLLAPSED
        mExpansion = expansion
        requestLayout()
        mExpansionUpdateBlock?.invoke(expansion, currentState)
    }

    private inner class ExpansionListener(val targetExpansion: Float) : Animator.AnimatorListener {
        private var canceled = false

        override fun onAnimationStart(animation: Animator?) {
            currentState = if (targetExpansion == 0f) State.COLLAPSED else State.EXPANDING
        }

        override fun onAnimationEnd(animation: Animator?) {
            if (!canceled) {
                currentState = if (targetExpansion == 0f) State.COLLAPSED else State.EXPANDED
                setExpansion(targetExpansion)
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
            canceled = true
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }

    }

}