package com.xyoye.player.controller.setting

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.xyoye.common_component.utils.dp2px
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.wrapper.ControlWrapper


/**
 * Created by xyoye on 2022/1/10
 */
abstract class BaseSettingView<V: ViewDataBinding>(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterSettingView {
    protected lateinit var mControlWrapper: ControlWrapper

    private val settingGravity by lazy { getGravity() }
    private val settingLayoutId by lazy { getLayoutId() }
    private val parentView by lazy { this }

    private val defaultSettingWidth: Float = when(settingGravity){
        Gravity.END -> dp2px(300).toFloat()
        Gravity.START -> -dp2px(300).toFloat()
        else -> throw IllegalArgumentException("Illegal setting view gravity: ${javaClass.simpleName}")
    }

    protected val viewBinding = DataBindingUtil.inflate<V>(
        LayoutInflater.from(context),
        settingLayoutId,
        parentView,
        true
    )!!

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView(): View {
        return this
    }

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            ViewCompat.animate(viewBinding.root)
                .translationX(0f)
                .setDuration(500)
                .start()
        } else {
            ViewCompat.animate(viewBinding.root)
                .translationX(defaultSettingWidth)
                .setDuration(500)
                .start()
        }
    }

    override fun isSettingShowing(): Boolean {
        return viewBinding.root.translationX == 0f
    }

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    open fun getGravity() = Gravity.END

    abstract fun getLayoutId(): Int
}