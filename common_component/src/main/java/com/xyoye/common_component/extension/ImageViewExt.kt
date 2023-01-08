package com.xyoye.common_component.extension

import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import coil.request.videoFramePercent
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.xyoye.common_component.R
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.coil.CoilLoadCompleteTarget
import com.xyoye.common_component.utils.coil.CoilPaletteTarget
import com.xyoye.common_component.utils.dp2px

/**
 * Created by xyoye on 2020/7/31.
 */

fun ImageView.loadImage(source: String?) {
    load(source) {
        scale(Scale.FILL)
        crossfade(true)
    }
}

fun ImageView.loadImage(source: String?, dpRadius: Int) {
    val radius = dp2px(dpRadius).toFloat()
    val transformation = RoundedCornersTransformation(radius)
    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        transformations(transformation)
    }
}

fun ImageView.loadImageWithPalette(source: String?, onPaletteColor: (Int) -> Unit) {
    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        allowHardware(false)
        target(CoilPaletteTarget(this@loadImageWithPalette, onPaletteColor))
    }
}

fun ImageView.loadImageWithCallback(
    source: String?,
    dpRadius: Float = 0f,
    errorRes: Int = 0,
    onComplete: () -> Unit
) {
    val transformation = if (dpRadius > 0)
        RoundedCornersTransformation(dpRadius.px())
    else
        null

    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        error(errorRes)
        transformation?.let {
            transformations(it)
        }
        target(CoilLoadCompleteTarget(this@loadImageWithCallback, onComplete))
    }
}

fun ImageView.loadImage(file: StorageFile) {
    var diskCachePolicy = CachePolicy.ENABLED
    var memoryCachePolicy = CachePolicy.ENABLED

    val coverCacheFile = file.uniqueKey().toCoverFile()
    val source = if (coverCacheFile.isValid()) {
        diskCachePolicy = CachePolicy.DISABLED
        memoryCachePolicy = CachePolicy.DISABLED
        coverCacheFile
    } else if (file.fileUrl().isEmpty().not()) {
        file.fileUrl()
    } else {
        R.drawable.ic_dandanplay
    }

    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        transformations(RoundedCornersTransformation(5f.px()))
        diskCachePolicy(diskCachePolicy)
        memoryCachePolicy(memoryCachePolicy)
        videoFramePercent(0.1)

        // todo 加载URL视频的第一帧
    }
}

@JvmName("-deprecated_uri")
@Deprecated(
    message = "moved to loadImage()",
    replaceWith = ReplaceWith(expression = "loadImage()"),
    level = DeprecationLevel.WARNING
)
fun ImageView.setVideoCover(uniqueKey: String?, placeholder: String? = null) {
    val coverFile = uniqueKey.toCoverFile()
    var diskCachePolicy = CachePolicy.ENABLED
    var memoryCachePolicy = CachePolicy.ENABLED
    val image = if (coverFile.isValid()) {
        diskCachePolicy = CachePolicy.DISABLED
        memoryCachePolicy = CachePolicy.DISABLED
        coverFile
    } else placeholder ?: R.drawable.ic_dandanplay

    val radius = dp2px(5).toFloat()
    val transformation = RoundedCornersTransformation(radius)
    load(image) {
        scale(Scale.FILL)
        crossfade(true)
        placeholder(R.drawable.ic_dandanplay)
        transformations(transformation)
        diskCachePolicy(diskCachePolicy)
        memoryCachePolicy(memoryCachePolicy)
        videoFramePercent(0.1)
    }
}