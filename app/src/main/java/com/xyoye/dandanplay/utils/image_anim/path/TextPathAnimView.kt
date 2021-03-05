package com.xyoye.dandanplay.utils.image_anim.path

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.xyoye.dandanplay.R

/**
 * Created by xyoye on 2019/9/15.
 */

class TextPathAnimView : View {
    private val mTextBgColor: Int
    private val mTextFgColor: Int
    private val mIsLoop: Boolean
    private val mAnimDuration: Int
    private val mSourcePath: Path
    private val mSourceTextPath: TextPath

    private var mPaddingLeft = 0
    private var mPaddingTop = 0

    private val mPaint = Paint()
    private val mAnimPath = Path()
    private val mPathMeasure = PathMeasure()
    private val mAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)

    private var animListener: AnimListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mPaint.run {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        context.obtainStyledAttributes(
            attrs,
            R.styleable.TextPathAnimView
        ).run {
            mAnimDuration = getInt(R.styleable.TextPathAnimView_duration, 1500)
            mTextBgColor = getColor(R.styleable.TextPathAnimView_text_bg_color, Color.BLACK)
            mTextFgColor = getColor(R.styleable.TextPathAnimView_text_fg_color, Color.WHITE)
            mIsLoop = getBoolean(R.styleable.TextPathAnimView_loop, true)
            val contentText = getString(R.styleable.TextPathAnimView_text) ?: ""
            val sizeScale = getFloat(R.styleable.TextPathAnimView_text_size_scale, dp2px(14f))
            val stokeWidth = getFloat(R.styleable.TextPathAnimView_text_stoke_width, 3f)
            val textInterval = getDimension(R.styleable.TextPathAnimView_text_interval, dp2px(5f))

            mSourceTextPath = TextPath(contentText, sizeScale, textInterval)
            mSourcePath = mSourceTextPath.path
            mPaint.strokeWidth = stokeWidth
            recycle()
        }

        initAnim()
    }

    private fun initAnim() {
        mAnimator.run {
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener { animation: ValueAnimator ->
                val stopD = mPathMeasure.length * animation.animatedValue as Float
                mPathMeasure.getSegment(0f, stopD, mAnimPath, true)
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    mPathMeasure.getSegment(
                        0f,
                        mPathMeasure.length,
                        mAnimPath,
                        true
                    )
                    mPathMeasure.nextContour()
                    if (mPathMeasure.length == 0f) {
                        if (mIsLoop) {
                            mAnimPath.reset()
                            mAnimPath.lineTo(0f, 0f)
                            mPathMeasure.setPath(mSourcePath, false)
                            animListener?.onLoop()
                        } else {
                            animation.end()
                        }
                    }
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    animListener?.onStart()
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!mIsLoop) {
                        animListener?.onEnd()
                    }
                }
            })
        }
    }

    private fun dp2px(dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mPaddingLeft = paddingLeft
        mPaddingTop = paddingTop
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val originWith = MeasureSpec.getSize(widthMeasureSpec)
        val originHeight = MeasureSpec.getSize(heightMeasureSpec)
        val newWidth = mSourceTextPath.width.toInt()
        val newHeight = mSourceTextPath.height.toInt()
        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT
            && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT)
        {
            setMeasuredDimension(newWidth, newHeight)
        } else if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(newWidth, originHeight)
        } else if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(originWith, newHeight)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(mPaddingLeft.toFloat(), mPaddingTop.toFloat())
        mPaint.color = mTextBgColor
        canvas.drawPath(mSourcePath, mPaint)
        mPaint.color = mTextFgColor
        canvas.drawPath(mAnimPath, mPaint)
    }

    fun startAnim() {
        if (mAnimator.isRunning) {
            return
        }
        mAnimPath.reset()
        mAnimPath.lineTo(0f, 0f)
        mPathMeasure.setPath(mSourcePath, false)
        var count = 0
        while (mPathMeasure.length != 0f) {
            mPathMeasure.nextContour()
            count++
        }
        mPathMeasure.setPath(mSourcePath, false)
        mAnimator.duration = mAnimDuration / count.toLong()
        mAnimator.start()
    }

    fun stopAnim() {
        if (mAnimator.isRunning) {
            mAnimator.end()
        }
    }

    fun setAnimListener(listener: AnimListener?) {
        animListener = listener
    }

    interface AnimListener {
        fun onStart()
        fun onEnd()
        fun onLoop()
    }
}