package com.xyoye.common_component.weight

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.R
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.databinding.LayoutLevelToastBinding
import com.xyoye.common_component.extension.isNightMode

/**
 * Created by xyoye on 2020/9/7.
 */

object ToastCenter {

    enum class Level {
        SUCCESS,
        ERROR,
        WARNING,
        INFO,
        DELETE
    }

    enum class ShowType {
        DARK,
        COLOR,
        DAY_NIGHT,
        DAY_NAIGHT_REVERSE,
        ORIGINAL
    }

    private val mLevelTitle = mapOf(
        Pair(Level.SUCCESS, "成功"),
        Pair(Level.ERROR, "错误"),
        Pair(Level.WARNING, "警告"),
        Pair(Level.INFO, "提示"),
        Pair(Level.DELETE, "提示")
    )

    private val mLevelColor = mapOf(
        Pair(Level.SUCCESS, R.color.success_color),
        Pair(Level.ERROR, R.color.error_color),
        Pair(Level.WARNING, R.color.warning_color),
        Pair(Level.INFO, R.color.info_color),
        Pair(Level.DELETE, R.color.delete_color)
    )

    private val mLevelDrawable = mapOf(
        Pair(Level.SUCCESS, R.drawable.ic_toast_success),
        Pair(Level.ERROR, R.drawable.ic_toast_error),
        Pair(Level.WARNING, R.drawable.ic_toast_warning),
        Pair(Level.INFO, R.drawable.ic_toast_info),
        Pair(Level.DELETE, R.drawable.ic_toast_delete)
    )

    private var isDarkMode = true

    fun showInfo(
        message: String,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null
    ) {
        showToast(message, Level.INFO, toastDuration, title)
    }

    fun showSuccess(
        message: String,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null
    ) {
        showToast(message, Level.SUCCESS, toastDuration, title)
    }

    fun showWarning(
        message: String,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null
    ) {
        showToast(message, Level.WARNING, toastDuration, title)
    }

    fun showError(
        message: String,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null
    ) {
        showToast(message, Level.ERROR, toastDuration, title)
    }

    fun showDelete(
        message: String,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null
    ) {
        showToast(message, Level.DELETE, toastDuration, title)
    }

    fun showOriginalToast(
        message: String,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null
    ) {
        showToast(message, Level.INFO, toastDuration, title, ShowType.ORIGINAL)
    }

    fun showToast(
        message: String,
        level: Level = Level.INFO,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null,
        showType: ShowType = ShowType.DARK
    ) {
        val context = BaseApplication.getAppContext()
        isDarkMode = when (showType) {
            ShowType.DARK -> true
            ShowType.COLOR -> false
            ShowType.DAY_NIGHT -> !context.isNightMode()
            ShowType.DAY_NAIGHT_REVERSE -> context.isNightMode()
            ShowType.ORIGINAL -> {
                showOriginalToast(context, message, toastDuration)
                return
            }
        }

        BaseApplication.getMainHandler().post {
            makeAndShow(context, message, level, toastDuration, title)
        }
    }

    private fun makeAndShow(
        context: Context,
        message: String,
        level: Level = Level.INFO,
        toastDuration: Int = Toast.LENGTH_SHORT,
        title: String? = null
    ) {

        val toastBinding = DataBindingUtil.inflate<LayoutLevelToastBinding>(
            LayoutInflater.from(context),
            R.layout.layout_level_toast,
            null,
            false
        )

        initToastBackground(toastBinding.root, level)
        initToastImage(toastBinding.toastIv, level)
        initToastTitle(toastBinding.toastTitleTv, title, level)

        toastBinding.toastContentTv.text = message

        Toast(context).run {
            duration = toastDuration
            setGravity(Gravity.BOTTOM, 0, 100)
            view = toastBinding.root
            show()
        }
    }

    private fun initToastBackground(toastView: View, level: Level) {
        val context = toastView.context
        val backgroundColor = if (isDarkMode)
            ContextCompat.getColor(context, R.color.toast_dark_color)
        else
            ContextCompat.getColor(context, getLevelColor(level))

        toastView.background =
            ContextCompat.getDrawable(context, R.drawable.background_toast)?.also {
                it.colorFilter = PorterDuffColorFilter(
                    backgroundColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
    }

    private fun initToastImage(imageView: ImageView, level: Level) {
        val context = imageView.context

        val levelColor = ContextCompat.getColor(context, getLevelColor(level))
        val drawable = ContextCompat.getDrawable(context, getLevelDrawable(level))
        val imageAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_toast_image)

        if (drawable != null) {
            DrawableCompat.setTint(DrawableCompat.wrap(drawable), levelColor)
        }

        imageView.setImageDrawable(drawable)
        imageView.animation = imageAnimation
    }

    private fun initToastTitle(textView: TextView, title: String?, level: Level) {
        val context = textView.context

        val levelColor = if (isDarkMode) {
            ContextCompat.getColor(context, getLevelColor(level))
        } else {
            Color.WHITE
        }

        textView.apply {
            setTextColor(levelColor)
            text = title ?: mLevelTitle[level]
        }
    }

    private fun getLevelDrawable(level: Level): Int =
        mLevelDrawable[level] ?: R.drawable.ic_toast_info

    private fun getLevelColor(level: Level): Int = mLevelColor[level] ?: R.color.info_color

    private fun showOriginalToast(context: Context, message: String, duration: Int) {
        BaseApplication.getMainHandler().post {
            Toast.makeText(context, message, duration).apply {
                setText(message)
                show()
            }
        }
    }
}