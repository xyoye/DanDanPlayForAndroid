package com.xyoye.common_component.utils.coil

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.target.ImageViewTarget
import kotlin.math.roundToInt

/**
 * Created by xyoye on 2020/10/8.
 */

class CoilPaletteTarget(
    imageView: ImageView,
    private val onPaletteColor: (paletteColor: Int) -> Unit
) : ImageViewTarget(imageView) {

    private val defaultPalette = Color.parseColor("#121212")

    override fun onSuccess(result: Drawable) {
        super.onSuccess(result)
        Palette.from(result.toBitmap())
            .maximumColorCount(10)
            .generate {
                if (it != null) {
                    val vibrantColor = it.getVibrantColor(defaultPalette)
                    onPaletteColor.invoke(getTranslucentColor(vibrantColor))
                } else {
                    onPaletteColor.invoke(getTranslucentColor(defaultPalette))
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
}