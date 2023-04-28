package com.xyoye.storage_component.ui.weight

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.xyoye.common_component.utils.dp2px
import com.xyoye.storage_component.R
import kotlin.math.max
import kotlin.math.min

/**
 * Created by xyoye on 2021/3/20.
 */

class ScanWindowView : View {

    //扫描框颜色
    private var mFrameColor = Color.parseColor("#33000000")

    //扫描框宽度
    private var mFrameWidth = dp2px(5).toFloat()

    //扫描框圆角
    private var mFrameRadius = dp2px(10).toFloat()

    //动画扫描框渐变大小
    private var mFrameAnimatorSize = 0f

    //动画扫描框渐变宽度
    private var mFrameAnimatorWidth = mFrameWidth

    //动画扫描框渐变颜色
    private var mFrameAnimatorColor = Color.WHITE

    //动画扫描框时长
    private var mFrameAnimatorDuration = 2000

    //动画扫描框原始颜色
    private var mAnimatorColor = mFrameAnimatorColor

    //窗口大小
    private var mWindowSize = 0f

    //窗口距离顶部高度
    private var mWindowMarginTop = 0f

    private lateinit var mWindowPaint: Paint
    private lateinit var mWindowFramePaint: Paint
    private lateinit var mAnimatorFramePaint: Paint

    private var mRippleAnimator: ValueAnimator? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val attribute = context.obtainStyledAttributes(attrs, R.styleable.ScanWindowView)
        mWindowSize = attribute.getDimension(R.styleable.ScanWindowView_window_size, 0f)
        mWindowMarginTop = attribute.getDimension(R.styleable.ScanWindowView_window_margin_top, 0f)

        mFrameColor = attribute.getColor(R.styleable.ScanWindowView_frame_color, mFrameColor)
        mFrameWidth = attribute.getDimension(R.styleable.ScanWindowView_frame_width, mFrameWidth)
        mFrameRadius = attribute.getDimension(R.styleable.ScanWindowView_frame_radius, mFrameRadius)

        mAnimatorColor =
            attribute.getColor(R.styleable.ScanWindowView_animator_color, mAnimatorColor)
        mFrameAnimatorDuration =
            attribute.getInt(R.styleable.ScanWindowView_animator_duration, mFrameAnimatorDuration)
        attribute.recycle()

        mWindowFramePaint = Paint().apply {
            color = mFrameColor
            style = Paint.Style.STROKE
            strokeWidth = mFrameWidth
        }

        mAnimatorFramePaint = Paint().apply {
            color = mFrameColor
            style = Paint.Style.STROKE
            strokeWidth = mFrameWidth
        }

        mWindowPaint = Paint().apply {
            //扫描框的背景也要清除
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = mFrameWidth
            strokeJoin = Paint.Join.ROUND
            //清除背景
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    override fun onDetachedFromWindow() {
        mRippleAnimator?.cancel()
        mRippleAnimator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            super.onDraw(canvas)
            return
        }

        val frameSize = getFrameSize()

        //清空扫描框背景
        drawFrame(canvas, frameSize, mWindowPaint)

        //绘制原始扫描框（半透明）
        mWindowFramePaint.color = mFrameColor
        drawFrame(canvas, frameSize, mWindowFramePaint)

        //根据动画值绘制渐变的扫描框
        mAnimatorFramePaint.color = mFrameAnimatorColor
        mAnimatorFramePaint.strokeWidth = mFrameAnimatorWidth
        drawFrame(canvas, mFrameAnimatorSize, mAnimatorFramePaint)

        //第一次绘制时开启动画
        if (mRippleAnimator == null) {
            initAnimator()
        }

        super.onDraw(canvas)
    }

    private fun drawFrame(canvas: Canvas, frameSize: Float, paint: Paint) {
        //默认扫描框居中
        val bottom = getFrameTop(frameSize)
        val right = (width - frameSize) / 2f
        val left = right + frameSize
        val top = bottom + frameSize

        val frameRectF = RectF(left, top, right, bottom)
        canvas.drawRoundRect(frameRectF, mFrameRadius, mFrameRadius, paint)
    }

    private fun initAnimator() {
        val rippleWidth = dp2px(150).toFloat()

        mRippleAnimator = ValueAnimator.ofFloat(0f, 1f)
            .apply {
                repeatCount = -1
                duration = mFrameAnimatorDuration.toLong()
                repeatMode = ValueAnimator.RESTART
                interpolator = FastOutSlowInInterpolator()
                addUpdateListener {
                    //改变大小
                    val animatorValue = it.animatedValue as Float
                    mFrameAnimatorSize = getFrameSize() + (animatorValue * rippleWidth)

                    //改变颜色
                    val alpha = (1 - animatorValue) * 255
                    val red = Color.red(mAnimatorColor)
                    val green = Color.green(mAnimatorColor)
                    val blue = Color.blue(mAnimatorColor)
                    mFrameAnimatorColor = Color.argb(alpha.toInt(), red, green, blue)

                    //改变宽度
                    mFrameAnimatorWidth = (1 - animatorValue) * mFrameWidth

                    postInvalidate()
                }
                start()
            }
    }

    /**
     * 获取扫描框大小，默认为view宽度的4/5
     */
    private fun getFrameSize(): Float {
        return if (mWindowSize == 0f) {
            (min(width, height)) / 5f * 4f
        } else {
            mWindowSize
        }
    }


    /**
     * 获取扫描框距离顶部高度，默认居中
     */
    private fun getFrameTop(frameSize: Float): Float {
        return if (mWindowMarginTop == 0f) {
            (max(width, height) - frameSize) / 2f
        } else {
            mWindowMarginTop
        }
    }

    fun getRectF(): RectF {
        val frameSize = getFrameSize()

        val bottom = getFrameTop(frameSize)
        val right = (width - frameSize) / 2f
        val left = right + frameSize
        val top = bottom + frameSize

        return RectF(left, top, right, bottom)
    }
}