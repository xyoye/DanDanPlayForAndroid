/*
 * Copyright (C) 2016 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.xyoye.dandanplay.utils.image_anim.svg

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import com.xyoye.dandanplay.R

/**
 * Animated SVG Drawing for Android
 */
class AnimatedSvgView : View {
    private var mTraceTime = 2000
    private var mTraceTimePerGlyph = 1000
    private var mFillStart = 1200
    private var mFillTime = 1000
    private var mViewportWidth = 0f
    private var mViewportHeight = 0f
    private var aspectRatioWidth = 1f
    private var aspectRatioHeight = 1f
    private var mMarkerLength = 0f
    private var mWidth = 0
    private var mHeight = 0
    private var mStartTime: Long = 0

    private lateinit var mFillColors: IntArray
    private lateinit var mGlyphStrings: Array<String>

    private var mFillPaint = Paint()
    private var mTraceResidueColors = intArrayOf(0x32000000)
    private var mTraceColors = intArrayOf(Color.BLACK)
    private var mViewport = PointF(mViewportWidth, mViewportHeight)
    private val mGlyphData = mutableListOf<GlyphData>()


    /**
     * Get the animation state.
     *
     * @return Either {[.STATE_NOT_STARTED],
     * [.STATE_TRACE_STARTED]},
     * [.STATE_FILL_STARTED] or
     * [.STATE_FINISHED]
     */
    private var state = STATE_NOT_STARTED
    private var mOnStateChangeListener: OnStateChangeListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mFillPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        if (attrs != null) {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AnimatedSvgView
            ).run {
                mViewportWidth = getFloat(R.styleable.AnimatedSvgView_animatedSvgImageSizeX, 512f)
                aspectRatioWidth = getFloat(R.styleable.AnimatedSvgView_animatedSvgImageSizeX, 512f)
                mViewportHeight = getFloat(R.styleable.AnimatedSvgView_animatedSvgImageSizeY, 512f)
                aspectRatioHeight =
                    getFloat(R.styleable.AnimatedSvgView_animatedSvgImageSizeY, 512f)
                mTraceTime = getInt(R.styleable.AnimatedSvgView_animatedSvgTraceTime, 2000)
                mTraceTimePerGlyph =
                    getInt(R.styleable.AnimatedSvgView_animatedSvgTraceTimePerGlyph, 1000)
                mFillStart = getInt(R.styleable.AnimatedSvgView_animatedSvgFillStart, 1200)
                mFillTime = getInt(R.styleable.AnimatedSvgView_animatedSvgFillTime, 1000)
                val traceMarkerLength =
                    getInt(R.styleable.AnimatedSvgView_animatedSvgTraceMarkerLength, 16)
                val glyphStringsId =
                    getResourceId(R.styleable.AnimatedSvgView_animatedSvgGlyphStrings, 0)
                val traceResidueColorsId =
                    getResourceId(R.styleable.AnimatedSvgView_animatedSvgTraceResidueColors, 0)
                val traceColorsId =
                    getResourceId(R.styleable.AnimatedSvgView_animatedSvgTraceColors, 0)
                val fillColorsId =
                    getResourceId(R.styleable.AnimatedSvgView_animatedSvgFillColors, 0)

                mMarkerLength = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    traceMarkerLength.toFloat(),
                    resources.displayMetrics
                )
                if (glyphStringsId != 0) {
                    mGlyphStrings = resources.getStringArray(glyphStringsId)
                    setTraceResidueColor(Color.argb(50, 0, 0, 0))
                    setTraceColor(Color.BLACK)
                }
                if (traceResidueColorsId != 0) {
                    mTraceResidueColors = resources.getIntArray(traceResidueColorsId)
                }
                if (traceColorsId != 0) {
                    mTraceColors = resources.getIntArray(traceColorsId)
                }
                if (fillColorsId != 0) {
                    mFillColors = resources.getIntArray(fillColorsId)
                }
                recycle()
            }

            mViewport = PointF(mViewportWidth, mViewportHeight)
        }
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        rebuildGlyphData()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (
            height <= 0
            && width <= 0
            && heightMode == MeasureSpec.UNSPECIFIED
            && widthMode == MeasureSpec.UNSPECIFIED
        ) {
            width = 0
            height = 0
        } else if (height <= 0 && heightMode == MeasureSpec.UNSPECIFIED) {
            height = (width * aspectRatioHeight / aspectRatioWidth).toInt()
        } else if (width <= 0 && widthMode == MeasureSpec.UNSPECIFIED) {
            width = (height * aspectRatioWidth / aspectRatioHeight).toInt()
        } else if (width * aspectRatioHeight > aspectRatioWidth * height) {
            width = (height * aspectRatioWidth / aspectRatioHeight).toInt()
        } else {
            height = (width * aspectRatioHeight / aspectRatioWidth).toInt()
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (state == STATE_NOT_STARTED) {
            return
        }
        val time = System.currentTimeMillis() - mStartTime
        // Draw outlines (starts as traced)
        for (i in mGlyphData.indices) {
            val phase = constrain(
                0f, 1f,
                (time - (mTraceTime - mTraceTimePerGlyph) * i * 1f / mGlyphData.size) * 1f / mTraceTimePerGlyph
            )

            val distance = INTERPOLATOR.getInterpolation(phase) * mGlyphData[i].length
            mGlyphData[i].paint.apply {
                color = mTraceResidueColors[i]
                pathEffect = DashPathEffect(
                    floatArrayOf(
                        distance,
                        mGlyphData[i].length
                    ),
                    0f
                )
            }
            canvas.drawPath(mGlyphData[i].path, mGlyphData[i].paint)

            mGlyphData[i].paint.apply {
                mTraceColors[i]
                pathEffect = DashPathEffect(
                    floatArrayOf(
                        0f,
                        distance,
                        if (phase > 0) mMarkerLength else 0f,
                        mGlyphData[i].length
                    ),
                    0f
                )
            }
            canvas.drawPath(mGlyphData[i].path, mGlyphData[i].paint)
        }

        if (time > mFillStart) {
            if (state < STATE_FILL_STARTED) {
                changeState(STATE_FILL_STARTED)
            }
            // If after fill start, draw fill
            val phase =
                constrain(0f, 1f, (time - mFillStart) * 1f / mFillTime)
            for (i in mGlyphData.indices) {
                val glyphData = mGlyphData[i]
                val fillColor = mFillColors[i]
                val a = (phase * (Color.alpha(fillColor).toFloat() / 255.toFloat()) * 255).toInt()
                val r = Color.red(fillColor)
                val g = Color.green(fillColor)
                val b = Color.blue(fillColor)
                mFillPaint.setARGB(a, r, g, b)
                canvas.drawPath(glyphData.path, mFillPaint)
            }
        }

        // draw next frame if animation isn't finished
        if (time < mFillStart + mFillTime) {
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            changeState(STATE_FINISHED)
        }
    }

    /**
     * If you set the SVG data paths more than once using [.setGlyphStrings] you should call this method
     * before playing the animation.
     */
    private fun rebuildGlyphData() {
        val X = mWidth / mViewport.x
        val Y = mHeight / mViewport.y
        val scaleMatrix = Matrix()
        val outerRect = RectF(X, X, Y, Y)
        scaleMatrix.setScale(X, Y, outerRect.centerX(), outerRect.centerY())
        mGlyphData.clear()
        for (i in mGlyphStrings.indices) {
            val glyphData = GlyphData()
            glyphData.paint.run {
                style = Paint.Style.STROKE
                isAntiAlias = true
                color = Color.WHITE
                strokeWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1f,
                    resources.displayMetrics
                )
            }

            try {
                glyphData.path = PathParser.createPathFromPathData(mGlyphStrings[i])!!
                glyphData.path.transform(scaleMatrix)
            } catch (e: Exception) {
                glyphData.path = Path()
                Log.e(TAG, "Couldn't parse path", e)
            }
            val pm = PathMeasure(glyphData.path, true)
            while (true) {
                glyphData.length = glyphData.length.coerceAtLeast(pm.length)
                if (!pm.nextContour()) {
                    break
                }
            }

            mGlyphData.add(glyphData)
        }
    }

    /**
     * Set the viewport width and height of the SVG. This can be found in the viewBox in the SVG. This is not the size
     * of the view.
     *
     * @param viewportWidth  the width
     * @param viewportHeight the height
     */
    fun setViewportSize(viewportWidth: Float, viewportHeight: Float) {
        mViewportWidth = viewportWidth
        mViewportHeight = viewportHeight
        aspectRatioWidth = viewportWidth
        aspectRatioHeight = viewportHeight
        mViewport = PointF(mViewportWidth, mViewportHeight)
        requestLayout()
    }

    /**
     * Set the color used for tracing. This will be applied to all data paths.
     *
     * @param color The color
     */
    private fun setTraceResidueColor(@ColorInt color: Int) {
        val length = mGlyphStrings.size
        val colors = IntArray(length)
        for (i in 0 until length) {
            colors[i] = color
        }
        mTraceResidueColors = colors
    }

    /**
     * Set the color used for tracing. This will be applied to all data paths.
     *
     * @param color The color
     */
    private fun setTraceColor(@ColorInt color: Int) {
        val length = mGlyphStrings.size
        val colors = IntArray(length)
        for (i in 0 until length) {
            colors[i] = color
        }
        mTraceColors = colors
    }

    /**
     * Set the color used for the icon. This will apply the color to all SVG data paths.
     *
     * @param color The color
     */
    fun setFillColor(@ColorInt color: Int) {
        val length = mGlyphStrings.size
        val colors = IntArray(length)
        for (i in 0 until length) {
            colors[i] = color
        }
        mFillColors = colors
    }

    /**
     * Set the animation trace time
     *
     * @param traceTime time in milliseconds
     */
    fun setTraceTime(traceTime: Int) {
        mTraceTime = traceTime
    }

    /**
     * Set the time used to trace each glyph
     *
     * @param traceTimePerGlyph time in milliseconds
     */
    fun setTraceTimePerGlyph(traceTimePerGlyph: Int) {
        mTraceTimePerGlyph = traceTimePerGlyph
    }

    /**
     * Set the time at which colors will start being filled after the tracing begins
     *
     * @param fillStart time in milliseconds
     */
    fun setFillStart(fillStart: Int) {
        mFillStart = fillStart
    }

    /**
     * Set the time it takes to fill colors
     *
     * @param fillTime time in milliseconds
     */
    fun setFillTime(fillTime: Int) {
        mFillTime = fillTime
    }

    /**
     * Start the animation
     */
    fun start() {
        mStartTime = System.currentTimeMillis()
        changeState(STATE_TRACE_STARTED)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Reset the animation
     */
    fun reset() {
        mStartTime = 0
        changeState(STATE_NOT_STARTED)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Draw the SVG, skipping any animation.
     */
    fun setToFinishedFrame() {
        mStartTime = 1
        changeState(STATE_FINISHED)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Get notified about the animation states.
     *
     * @param onStateChangeListener The [OnStateChangeListener]
     */
    fun setOnStateChangeListener(onStateChangeListener: OnStateChangeListener?) {
        mOnStateChangeListener = onStateChangeListener
    }

    private fun changeState(@State state: Int) {
        if (this.state == state) {
            return
        }
        this.state = state
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener!!.onStateChange(state)
        }
    }

    /**
     * Callback for listening to animation state changes
     */
    interface OnStateChangeListener {
        /**
         * Called when the animation state changes.
         *
         * @param state The state of the animation.
         * Either {[.STATE_NOT_STARTED],
         * [.STATE_TRACE_STARTED]},
         * [.STATE_FILL_STARTED] or
         * [.STATE_FINISHED]
         */
        fun onStateChange(@State state: Int)
    }

    @IntDef(
        STATE_NOT_STARTED,
        STATE_TRACE_STARTED,
        STATE_FILL_STARTED,
        STATE_FINISHED
    )
    annotation class State

    internal class GlyphData {
        var path = Path()
        var paint = Paint()
        var length = 0f
    }

    companion object {
        /**
         * The animation has been reset or hasn't started yet.
         */
        const val STATE_NOT_STARTED = 0
        /**
         * The SVG is being traced
         */
        const val STATE_TRACE_STARTED = 1
        /**
         * The SVG has been traced and is now being filled
         */
        const val STATE_FILL_STARTED = 2
        /**
         * The animation has finished
         */
        const val STATE_FINISHED = 3
        private const val TAG = "AnimatedSvgView"
        private val INTERPOLATOR: Interpolator = DecelerateInterpolator()

        private fun constrain(min: Float, max: Float, v: Float): Float {
            return min.coerceAtLeast(max.coerceAtMost(v))
        }
    }
}