package com.xyoye.anime_component.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.palette.graphics.Palette
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.math.roundToInt

/**
 * Created by xyoye on 2020/10/8.
 */

abstract class PaletteBitmapTarget(private var defaultPalette: Int = -1) : CustomTarget<Bitmap>() {

    companion object {
        private val paletteColor = Color.parseColor("#121212")
    }

    init {
        defaultPalette = if (defaultPalette == -1) paletteColor else defaultPalette
    }

    override fun onLoadCleared(placeholder: Drawable?) {

    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        Palette.from(resource)
            .maximumColorCount(10)
            .generate {
                if (it != null) {
                    val vibrantColor = it.getVibrantColor(defaultPalette)
                    onBitmapReady(resource, getTranslucentColor(vibrantColor))
                } else {
                    onBitmapReady(resource, getTranslucentColor(defaultPalette))
                }
            }
    }

    private fun getTranslucentColor(rgb: Int, percent: Float = 0.7f): Int {
        val blue: Int = Color.blue(rgb)
        val green: Int = Color.green(rgb)
        val red: Int = Color.red(rgb)
        var alpha: Int = Color.alpha(rgb)
        alpha = (alpha * percent).roundToInt()
        return Color.argb(alpha, red, green, blue)
    }

    abstract fun onBitmapReady(bitmap: Bitmap, @ColorInt paletteColor: Int)
}