package com.xyoye.storage_component.ui.weight

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.xyoye.common_component.utils.dp2px
import com.xyoye.storage_component.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


/**
 * Created by xyoye on 2021/3/29.
 */

class RemoteControlView : AppCompatImageView {

    //绘制模式：上、下、左、右
    private var mMode: Int = 4

    //点击时颜色
    private var mPressedColor: Int = Color.RED

    //正常背景颜色
    private var mColor: Int = Color.BLUE

    //圆的边框颜色
    private var mCenterStrokeColor = Color.WHITE

    //是否处于点击状态
    private var mIsPressed = false

    //外圆
    private var fullSectorRectF = RectF(0f, 0f, 0f, 0f)

    //内圆
    private var innerSectorRectF = RectF(0f, 0f, 0f, 0f)

    private var sectorPath = Path()

    private lateinit var mPaint: Paint
    private lateinit var mStrokePaint: Paint

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val attribute = context.obtainStyledAttributes(attrs, R.styleable.RemoteControlView)
        mMode = attribute.getInt(R.styleable.RemoteControlView_control_mode, mMode)
        mColor = attribute.getColor(R.styleable.RemoteControlView_control_color, mColor)
        mPressedColor =
            attribute.getColor(R.styleable.RemoteControlView_control_pressed_color, mPressedColor)
        mCenterStrokeColor =
            attribute.getColor(R.styleable.RemoteControlView_center_stroke_color, mCenterStrokeColor)
        attribute.recycle()

        mPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = dp2px(1).toFloat()
            isAntiAlias = true
        }
        mStrokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = dp2px(1).toFloat()
            color = mCenterStrokeColor
            isAntiAlias = true
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //根据父布局宽高，调整圆弧宽高
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        when (mMode) {
            0, 1 -> {
                val newWidth = width / 2f * sqrt(2f)
                setMeasuredDimension(newWidth.toInt(), height)
            }
            2, 3 -> {
                val newHeight = height / 2f * sqrt(2f)
                setMeasuredDimension(width, newHeight.toInt())
            }
            else -> {
                val newSize = width * sqrt(2f)
                setMeasuredDimension(newSize.toInt(), newSize.toInt())
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        //较长的边记为宽，较短为高
        val calcWidth = max(measuredHeight, measuredWidth).toFloat()
        val calcHeight = min(measuredHeight, measuredWidth).toFloat()

        canvas?.let {
            if (mMode == 4) {
                drawCenterCircle(it)
            } else {
                drawSector(it, calcWidth, calcHeight)
            }
        }

        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || !isTouchValid(event)) {
            mIsPressed = false
            invalidate()
            return false
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            mIsPressed = true
            invalidate()
        } else if (event.action == MotionEvent.ACTION_UP) {
            mIsPressed = false
            invalidate()
        }
        return super.onTouchEvent(event)
    }

    /**
     * 画扇形
     */
    private fun drawSector(canvas: Canvas, calcWidth: Float, calcHeight: Float) {
        //扇形越过角度为90
        val angle = 360 / 4f

        //圆心X, Y
        var circleCenterX = 0f
        var circleCenterY = 0f
        //起始角度
        var startAngle = 0f

        when (mMode) {
            0 -> {
                //上
                startAngle = -135f
                circleCenterX = width / 2f
                circleCenterY = calcWidth / sqrt(2f)
            }
            1 -> {
                //下
                startAngle = 45f
                circleCenterX = width / 2f
                circleCenterY = calcHeight - (calcWidth / sqrt(2f))
            }
            2 -> {
                //左
                startAngle = 135f
                circleCenterX = calcWidth / sqrt(2f)
                circleCenterY = height / 2f
            }
            3 -> {
                //右
                startAngle = -45f
                circleCenterX = calcHeight - (calcWidth / sqrt(2f))
                circleCenterY = height / 2f
            }
        }

        //外圆半径
        val outerRadius = calcWidth / sqrt(2f)

        fullSectorRectF.set(
            (circleCenterX - outerRadius),
            (circleCenterY - outerRadius),
            (circleCenterX + outerRadius),
            (circleCenterY + outerRadius)
        )

        //内圆半径
        val innerRadius = (outerRadius - calcHeight) * sqrt(2f)

        innerSectorRectF.set(
            (circleCenterX - innerRadius),
            (circleCenterY - innerRadius),
            (circleCenterX + innerRadius),
            (circleCenterY + innerRadius)
        )

        //路径
        sectorPath.arcTo(fullSectorRectF, startAngle, angle)
        sectorPath.arcTo(innerSectorRectF, (startAngle + angle), (-angle))
        sectorPath.close()

        //不同点击状态下颜色
        if (mIsPressed) {
            mPaint.color = mPressedColor
        } else {
            mPaint.color = mColor
        }
        canvas.drawPath(sectorPath, mPaint)
    }

    /**
     * 画圆
     */
    private fun drawCenterCircle(canvas: Canvas) {
        val radius = measuredWidth / 2f - 1f
        val circleCenterX = measuredWidth / 2f
        val circleCenterY = measuredHeight / 2f

        //路径
        sectorPath.addCircle(circleCenterX, circleCenterY, radius, Path.Direction.CW)
        sectorPath.close()

        //不同点击状态下颜色
        if (mIsPressed) {
            mPaint.color = mPressedColor
        } else {
            mPaint.color = mColor
        }
        canvas.drawPath(sectorPath, mPaint)
        canvas.drawCircle(circleCenterX, circleCenterY, radius, mStrokePaint)
    }

    /***
     * 判断点击是否在路径区域内
     */
    private fun isTouchValid(event: MotionEvent): Boolean {
        val bounds = RectF()
        val region = Region()

        sectorPath.computeBounds(bounds, true)
        region.setPath(
            sectorPath,
            Region(
                Rect(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt()
                )
            )
        )

        return region.contains(event.x.toInt(), event.y.toInt())
    }
}