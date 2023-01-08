package com.xyoye.common_component.utils.coil

import android.graphics.drawable.Drawable
import android.widget.ImageView
import coil.target.ImageViewTarget

/**
 * Created by xyoye on 2023/1/8.
 */

class CoilLoadCompleteTarget(
    imageView: ImageView,
    private val onComplete: () -> Unit
) : ImageViewTarget(imageView) {
    override fun onError(error: Drawable?) {
        super.onError(error)
        onComplete.invoke()
    }

    override fun onSuccess(result: Drawable) {
        super.onSuccess(result)
        onComplete.invoke()
    }
}