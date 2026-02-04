package com.xyoye.player_component.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.xyoye.player_component.R

/**
 * Created by xyoye on 2021/11/16.
 */

class BatteryView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defaultStyle: Int = 0
) : View(context, attributeSet, defaultStyle) {

    //电极
    private val mHeaderWidth: Float
    private val mHeaderHeight: Float
    private val mHeaderRadius: Float
    private val mHeaderRectF = RectF(0f, 0f, 0f, 0f)
    private val mHeaderRadiusData: FloatArray
    private var mHeaderPath = Path()

    //外壳
    private val mOuterRadius: Float
    private val mOuterStroke: Float
    private val mOuterColor: Int
    private val mOuterRect = RectF(0f, 0f, 0f, 0f)

    //内部
    private val mInnerRadius: Float
    private val mInnerMargin: Float
    private val mInnerColor: Int
    private val mInnerRect = RectF(0f, 0f, 0f, 0f)
    private var mInnerWidth = 0f
    private var mInnerLeftOffset = 0f

    //文字
    private var mProgress = 0
    private val mTextColor: Int
    private val mTextSize: Float
    private var mTextBounds = Rect()
    private var mTextX = 0f
    private var mTextY = 0f
    private var mText = ""

    private val mHeaderPaint = Paint()
    private val mOuterPaint = Paint()
    private val mInnerPaint = Paint()
    private val mTextPaint = TextPaint()

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.BatteryView)
        mHeaderWidth = typedArray.getDimension(R.styleable.BatteryView_header_width, 0f)
        mHeaderHeight = typedArray.getDimension(R.styleable.BatteryView_header_height, 0f)
        mHeaderRadius = typedArray.getDimension(R.styleable.BatteryView_header_radius, mHeaderWidth)
        mOuterRadius = typedArray.getDimension(R.styleable.BatteryView_outer_radius, 0f)
        mOuterStroke = typedArray.getDimension(R.styleable.BatteryView_outer_stroke, 0f)
        mOuterColor = typedArray.getColor(R.styleable.BatteryView_outer_color, Color.WHITE)
        mInnerRadius = typedArray.getDimension(R.styleable.BatteryView_inner_radius, mOuterRadius)
        mInnerMargin = typedArray.getDimension(R.styleable.BatteryView_inner_margin, 0f)
        mInnerColor = typedArray.getColor(R.styleable.BatteryView_inner_color, Color.GRAY)
        mTextColor = typedArray.getColor(R.styleable.BatteryView_android_textColor, Color.WHITE)
        mTextSize = typedArray.getDimension(R.styleable.BatteryView_android_textSize, 0f)
        mProgress = typedArray.getInt(R.styleable.BatteryView_android_progress, 0)
        typedArray.recycle()

        mHeaderRadiusData = floatArrayOf(
            mHeaderRadius, mHeaderRadius, 0f, 0f, 0f, 0f, mHeaderRadius, mHeaderRadius
        )
        mHeaderRectF.right = mHeaderWidth
        mOuterRect.left = mHeaderWidth
        mInnerRect.top = (mOuterStroke / 2) + mInnerMargin

        mHeaderPaint.apply {
            color = mOuterColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        mOuterPaint.apply {
            color = mOuterColor
            isAntiAlias = true
            isDither = false
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            pathEffect = CornerPathEffect(mOuterRadius)
            strokeWidth = mOuterStroke
        }
        mInnerPaint.apply {
            color = mInnerColor
            isAntiAlias = true
            style = Paint.Style.FILL
            pathEffect = CornerPathEffect(mInnerRadius)
        }
        mTextPaint.apply {
            color = mTextColor
            textSize = mTextSize
            isAntiAlias = true
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        //调整电极位置
        mHeaderRectF.top = (height - mHeaderHeight) / 2f
        mHeaderRectF.bottom = mHeaderRectF.top + mHeaderHeight
        mHeaderPath = Path().apply {
            addRoundRect(mHeaderRectF, mHeaderRadiusData, Path.Direction.CW)
        }

        val outerStrokeInset = mOuterStroke / 2f

        //调整电池外壳大小
        mOuterRect.right = width.toFloat() - outerStrokeInset
        mOuterRect.bottom = height.toFloat() - outerStrokeInset

        //调整电池内容大小
        mInnerRect.right = width - mOuterStroke - mInnerMargin
        mInnerRect.left = mInnerRect.right
        mInnerRect.bottom = height - mOuterStroke - mInnerMargin
        mInnerWidth = width - mOuterStroke - (mInnerMargin * 2) - mHeaderWidth
        mInnerLeftOffset = mHeaderWidth + outerStrokeInset + mInnerMargin

        //更新进度
        setProgress(mProgress)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            drawPath(mHeaderPath, mHeaderPaint)
            drawRect(mOuterRect, mOuterPaint)
            drawRect(mInnerRect, mInnerPaint)
            drawText(mText, mTextX, mTextY, mTextPaint)
        }
    }

    private fun updateText() {
        mText = "$mProgress%"
        mTextPaint.getTextBounds(mText, 0, mText.length, mTextBounds)
        mTextX = mOuterRect.width() / 2f - mTextBounds.width() / 2f + mHeaderWidth
        mTextY = mOuterRect.height() / 2f + mTextBounds.height() / 2f
    }

    fun setBatteryColor(innerColor: Int, outerColor: Int, textColor: Int) {
        mInnerPaint.color = innerColor
        mOuterPaint.color = outerColor
        mHeaderPaint.color = outerColor
        mTextPaint.color = textColor
        invalidate()
    }

    fun resetBatteryColor() {
        mInnerPaint.color = mInnerColor
        mOuterPaint.color = mOuterColor
        mHeaderPaint.color = mOuterColor
        mTextPaint.color = mTextColor
        invalidate()
    }

    fun setChargingFraction(fraction: Float) {
        if (measuredWidth == 0 || mInnerWidth == 0f)
            return
        val unfilled = mInnerWidth * (1 - fraction)
        mInnerRect.left = mInnerLeftOffset + unfilled
        invalidate()
    }

    fun setProgress(progress: Int) {
        mProgress = progress
        updateText()
        setChargingFraction(progress / 100f)
        invalidate()
    }
}
