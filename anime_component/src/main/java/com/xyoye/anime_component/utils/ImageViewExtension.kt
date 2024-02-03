package com.xyoye.anime_component.utils

import android.widget.ImageView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.xyoye.anime_component.R

/**
 * Created by xyoye on 2024/2/1
 */

fun ImageView.loadAnimeCover(
    source: String?,
    radius: Float = resources.getDimension(R.dimen.anime_cover_radius)
) {
    load(source) {
        scale(Scale.FILL)
        placeholder(R.drawable.background_anime_cover_placeholder)
        error(R.drawable.background_anime_cover_error)
        crossfade(true)
        transformations(RoundedCornersTransformation(radius))
    }
}