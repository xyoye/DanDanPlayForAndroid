package com.xyoye.storage_component.ui.weight

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.xyoye.common_component.extension.colorWithAlpha
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.dp2px
import com.xyoye.storage_component.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Created by xyoye on 2022/7/23.
 * 雷达扫描图
 */

class RadarScanView : View {

    //背景线颜色
    private var mBackgroundLineColor = 0

    //背景线宽度
    private var mBackgroundLineWidth = 0f

    //扫描线颜色
    private var mScanLineColor = 0

    //扫描线宽度
    private var mScanLineWidth = 0f

    //扫描区域颜色
    private var mScanFrameColor = 0

    //扫描一圈时间
    private var mScanOnceTimeMs = 2500

    //扫描区域扇形的角度
    private val sweepAngle = 120f

    //点颜色
    private var mScanPointColor = 0

    //最小点数量
    private var mMinPointCount = 0

    //最大点数量
    private var mMaxPointCount = 0

    //扫描动画
    private var mScanAnimator: ValueAnimator? = null

    //扫描区域旋转角度
    private var angle = 0

    //扫描动画进度
    private var progress = 0f

    //点集合
    private val points = mutableListOf<RectF>()

    private lateinit var mBackgroundLinePaint: Paint
    private lateinit var mScanLinePaint: Paint
    private lateinit var mScanFramePaint: Paint
    private lateinit var mScanPointPaint: Paint

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        initAttribute(attrs)
        initPaint()
    }

    private fun initAttribute(attrs: AttributeSet?) {
        val attribute = context.obtainStyledAttributes(attrs, R.styleable.RadarScanView)
        mBackgroundLineColor = attribute.getColor(
            R.styleable.RadarScanView_background_line_color,
            R.color.radar_background_line_color.toResColor(context)
        )

        mBackgroundLineWidth = attribute.getDimension(
            R.styleable.RadarScanView_background_line_width,
            dp2px(1).toFloat()
        )

        mScanLineColor = attribute.getColor(
            R.styleable.RadarScanView_scan_line_color,
            R.color.radar_scan_line_color.toResColor(context)
        )
        mScanLineWidth = attribute.getDimension(
            R.styleable.RadarScanView_scan_line_width,
            dp2px(1.5f).toFloat()
        )

        mScanFrameColor = attribute.getColor(
            R.styleable.RadarScanView_scan_frame_color,
            R.color.radar_scan_frame_color.toResColor(context)
        )
        mScanPointColor = attribute.getColor(
            R.styleable.RadarScanView_scan_point_color,
            R.color.radar_scan_point_color.toResColor(context)
        )
        mScanOnceTimeMs = attribute.getInt(
            R.styleable.RadarScanView_scan_once_time,
            mScanOnceTimeMs
        )
        mMinPointCount = attribute.getInt(
            R.styleable.RadarScanView_scan_min_point,
            2
        )
        mMaxPointCount = attribute.getInt(
            R.styleable.RadarScanView_scan_max_point,
            7
        )
        attribute.recycle()
    }

    private fun initPaint() {
        mBackgroundLinePaint = Paint().apply {
            color = mBackgroundLineColor
            style = Paint.Style.STROKE
            strokeWidth = mBackgroundLineWidth
            isAntiAlias = true
        }

        mScanLinePaint = Paint().apply {
            color = mScanLineColor
            style = Paint.Style.STROKE
            strokeWidth = mScanLineWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        mScanFramePaint = Paint().apply {
            isAntiAlias = true
        }

        mScanPointPaint = Paint().apply {
            isAntiAlias = true
        }
    }

    override fun onDetachedFromWindow() {
        mScanAnimator?.cancel()
        mScanAnimator = null
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //扇形的渐变效果
        mScanFramePaint.shader = SweepGradient(
            width / 2f,
            height / 2f,
            intArrayOf(
                Color.TRANSPARENT,
                mScanFrameColor,
            ),
            floatArrayOf((360 - sweepAngle) / 360f, 1f)
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        //宽高相同
        val minSize = min(width, height)
        width = minSize
        height = minSize

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            super.onDraw(canvas)
            return
        }

        //背景
        drawBackgroundLine(canvas)
        //点
        drawPoints(canvas)
        //扫描区域
        drawScanFrame(canvas)

        //第一次绘制结束后开启动画
        if (mScanAnimator == null) {
            initAnimator()
        }

        super.onDraw(canvas)
    }

    private fun initAnimator() {
        mScanAnimator = ValueAnimator.ofInt(0, 360)
            .apply {
                repeatCount = -1
                duration = mScanOnceTimeMs.toLong()
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
                addUpdateListener {
                    angle = it.animatedValue as Int
                    progress = angle / 360f
                    postInvalidate()
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {
                        //创建点信息
                        createPoints()
                    }

                    override fun onAnimationEnd(p0: Animator) {

                    }

                    override fun onAnimationCancel(p0: Animator) {

                    }

                    override fun onAnimationRepeat(p0: Animator) {
                        //重置点信息
                        createPoints()
                    }

                })
                start()
            }
    }

    /**
     * 背景线
     */
    private fun drawBackgroundLine(canvas: Canvas) {
        //大圆
        val radius = min(width, height) / 2f - mBackgroundLineWidth
        canvas.drawCircle(width / 2f, height / 2f, radius, mBackgroundLinePaint)

        //小圆
        val smallCircleRadius = radius / 2f - mBackgroundLineWidth
        canvas.drawCircle(width / 2f, height / 2f, smallCircleRadius, mBackgroundLinePaint)

        //十字线
        canvas.drawLine(
            width / 2f,
            mBackgroundLineWidth,
            width / 2f,
            height.toFloat() - mBackgroundLineWidth,
            mBackgroundLinePaint
        )
        canvas.drawLine(
            mBackgroundLineWidth,
            height / 2f,
            width.toFloat() - mBackgroundLineWidth,
            height / 2f,
            mBackgroundLinePaint
        )
    }

    /**
     * 扫描区域
     */
    private fun drawScanFrame(canvas: Canvas) {
        //旋转画布
        canvas.rotate(angle.toFloat(), width / 2f, height / 2f)

        //扫描渐变区域
        canvas.drawArc(
            0f,
            0f,
            width.toFloat() - mBackgroundLineWidth,
            height.toFloat() - mBackgroundLineWidth,
            -sweepAngle,
            sweepAngle,
            true,
            mScanFramePaint
        )
        //扫描线
        canvas.drawLine(
            width / 2f + mBackgroundLineWidth,
            height / 2f,
            width - (mBackgroundLineWidth * 2),
            height / 2f,
            mScanLinePaint
        )
    }

    /**
     * 绘制点
     */
    private fun drawPoints(canvas: Canvas) {
        for (point in points) {
            //大小渐增
            val pSize = progress * point.width()

            //颜色透明度: 0 -> 255 -> 0
            val alphaProgress = if (progress - 0.5 > 0) 1 - progress else progress
            val pAlpha = (alphaProgress * 255).toInt()
            mScanPointPaint.color = mScanPointColor.colorWithAlpha(pAlpha)

            //绘制点
            canvas.drawCircle(point.left, point.top, pSize / 2, mScanPointPaint)
        }
    }

    /**
     * 创建需要显示点信息
     */
    private fun createPoints() {
        points.clear()

        //点所在范围圆的信息
        val maxRadius = min(width, height) / 2f - mBackgroundLineWidth
        val centerX = (width / 2f)
        val centerY = (height / 2f)
        //随机点数量
        val count = Random.nextInt(mMinPointCount, mMaxPointCount)

        for (i in 0..count) {
            //随机大小
            val rSize = Random.nextInt(10, 20)
            val size = dp2px(rSize).toFloat()
            //随机角度
            val rAngle = Random.nextFloat() * 360
            //随机在圆半径上位置
            val rRadius = Random.nextFloat() * (maxRadius - (size / 2))

            //计算点位置
            val x = centerX + cos(rAngle * Math.PI / 180).toFloat() * rRadius
            val y = centerY + sin(rAngle * Math.PI / 180).toFloat() * rRadius

            val point = RectF(x, y, x + size, y + size)
            points.add(point)
        }
    }
}