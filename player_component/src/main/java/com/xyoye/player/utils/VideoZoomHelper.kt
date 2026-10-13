package com.xyoye.player.utils

import android.os.Build
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.max

/**
 * Created by xyoye on 2026/10/12.
 *
 * 视频画面缩放助手
 *
 * 负责把「双指捏合缩放 / 放大后单指平移」的手势换算为渲染 View 的视图变换
 * （scaleX / scaleY + translationX / translationY），并处理边界约束与松手回弹。
 *
 * 说明：
 * 1. 仅改变渲染 View 的可视变换，不参与 RenderMeasureHelper 的尺寸测量，
 *    因此画面比例（VideoScreenScale）逻辑与缩放逻辑相互独立。
 * 2. 弹幕、字幕、控制层与本渲染 View 是同级兄弟节点，只变换渲染 View 即可
 *    保证这些覆盖层不跟随缩放。
 * 3. 渲染 View 能力差异见 [isSupported]。
 */
class VideoZoomHelper(
    private val targetProvider: () -> View?
) {

    companion object {
        /** 最小缩放倍数（同时也是复位目标值） */
        const val MIN_SCALE = 1f

        /** 最大缩放倍数 */
        const val MAX_SCALE = 4f

        /** 回弹 / 复位动画时长（毫秒） */
        private const val SETTLE_DURATION = 200L

        /** 判定「已放大」的阈值，避免浮点误差 */
        private const val ZOOMED_THRESHOLD = 0.01f

        /** SurfaceView 支持自由变换的最低系统版本（Android 7.0） */
        private const val SURFACE_TRANSFORM_MIN_API = Build.VERSION_CODES.N
    }

    /**
     * 当前缩放倍数
     */
    var scale = MIN_SCALE
        private set

    /**
     * 当前水平方向平移量
     */
    var translateX = 0f
        private set

    /**
     * 当前垂直方向平移量
     */
    var translateY = 0f
        private set

    private val interpolator = DecelerateInterpolator()

    /**
     * 当前渲染 View，可能为空（播放器尚未准备好）
     */
    val renderView: View?
        get() = targetProvider()

    /**
     * 当前渲染 View 是否支持自由变换
     *
     * - TextureView：全版本支持
     * - SurfaceView：需要 Android 7.0（API 24）及以上，低版本变换不生效
     * - 其它（如 VLC 的 VLCVideoLayout）：暂不支持
     */
    fun isSupported(): Boolean {
        return when (val view = renderView) {
            is TextureView -> true
            is SurfaceView -> Build.VERSION.SDK_INT >= SURFACE_TRANSFORM_MIN_API
            else -> false
        }
    }

    /**
     * 当前是否可响应缩放手势
     */
    fun canZoom(): Boolean = isSupported() && renderView?.isAttachedToWindow == true

    /**
     * 是否处于放大状态
     */
    fun isZoomed(): Boolean = scale > MIN_SCALE + ZOOMED_THRESHOLD

    /**
     * 以指定锚点进行增量缩放
     *
     * @param factor  本次缩放的增量倍数（ScaleGestureDetector.scaleFactor）
     * @param parentX 锚点在渲染 View 父容器坐标系中的 X（一般为手指焦点）
     * @param parentY 锚点在渲染 View 父容器坐标系中的 Y
     */
    fun zoomBy(factor: Float, parentX: Float, parentY: Float) {
        val view = renderView ?: return
        if (view.width == 0 || view.height == 0) {
            return
        }

        val oldScale = scale
        val newScale = (oldScale * factor).coerceIn(MIN_SCALE, MAX_SCALE)
        if (newScale == oldScale) {
            return
        }

        val centerX = view.width / 2f
        val centerY = view.height / 2f

        // 把父容器坐标反变换为「未经变换」的渲染 View 本地坐标
        val localX = centerX + (parentX - view.left - translateX - centerX) / oldScale
        val localY = centerY + (parentY - view.top - translateY - centerY) / oldScale

        // 保持锚点在屏幕上的位置不动：T1 = T0 + (a - center) * (S0 - S1)
        translateX += (localX - centerX) * (oldScale - newScale)
        translateY += (localY - centerY) * (oldScale - newScale)
        scale = newScale

        view.animate().cancel()
        applyTransform(view)
    }

    /**
     * 平移画面（放大后单指拖动）
     *
     * @param dx 水平位移，正值表示画面右移
     * @param dy 垂直位移，正值表示画面下移
     */
    fun translateBy(dx: Float, dy: Float) {
        if (!isZoomed()) {
            return
        }
        val view = renderView ?: return

        translateX += dx
        translateY += dy

        view.animate().cancel()
        applyTransform(view)
    }

    /**
     * 手势结束后的回弹处理
     *
     * 若缩放值或平移量越界，以动画回到合法范围。
     */
    fun settle() {
        val view = renderView ?: return
        if (view.width == 0 || view.height == 0) {
            reset(false)
            return
        }

        val targetScale = scale.coerceIn(MIN_SCALE, MAX_SCALE)
        val limits = computeTranslateLimit(view, targetScale)
        val targetTx = translateX.coerceIn(-limits[0], limits[0])
        val targetTy = translateY.coerceIn(-limits[1], limits[1])

        if (targetScale == scale && targetTx == translateX && targetTy == translateY) {
            return
        }

        scale = targetScale
        translateX = targetTx
        translateY = targetTy

        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .translationX(targetTx)
            .translationY(targetTy)
            .setDuration(SETTLE_DURATION)
            .setInterpolator(interpolator)
            .start()
    }

    /**
     * 复位缩放状态
     *
     * @param animate 是否使用动画过渡
     */
    fun reset(animate: Boolean) {
        val view = renderView

        scale = MIN_SCALE
        translateX = 0f
        translateY = 0f

        if (view == null) {
            return
        }

        if (animate) {
            view.animate()
                .scaleX(MIN_SCALE)
                .scaleY(MIN_SCALE)
                .translationX(0f)
                .translationY(0f)
                .setDuration(SETTLE_DURATION)
                .setInterpolator(interpolator)
                .start()
        } else {
            view.animate().cancel()
            applyTransform(view)
        }
    }

    /**
     * 应用当前缩放/平移到渲染 View，并做边界约束
     */
    private fun applyTransform(view: View) {
        if (view.width == 0 || view.height == 0) {
            return
        }

        val limits = computeTranslateLimit(view, scale)
        translateX = translateX.coerceIn(-limits[0], limits[0])
        translateY = translateY.coerceIn(-limits[1], limits[1])

        view.scaleX = scale
        view.scaleY = scale
        view.translationX = translateX
        view.translationY = translateY
    }

    /**
     * 计算指定缩放倍数下的平移边界
     *
     * @return [maxTranslateX, maxTranslateY]
     */
    private fun computeTranslateLimit(view: View, targetScale: Float): FloatArray {
        val maxTx = max(0f, (view.width * targetScale - view.width) / 2f)
        val maxTy = max(0f, (view.height * targetScale - view.height) / 2f)
        return floatArrayOf(maxTx, maxTy)
    }
}
