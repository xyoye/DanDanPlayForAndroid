package com.xyoye.common_component.extension

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.xyoye.common_component.R
import com.xyoye.common_component.utils.dp2px

/**
 * Created by xyoye on 2020/7/31.
 */

fun ImageView.setGlideImage(
    source: String?,
    dpRadius: Int = 0,
    @DrawableRes errorRes: Int = 0
) {
    if (dpRadius > 0) {
        Glide.with(this)
            .asBitmap()
            .error(errorRes)
            .load(source)
            .transition((BitmapTransitionOptions.withCrossFade()))
            .transform(CenterCrop(), RoundedCorners(dp2px(dpRadius)))
            .into(this)
    } else {
        Glide.with(this)
            .load(source)
            .error(errorRes)
            .transform(CenterCrop())
            .transition((DrawableTransitionOptions.withCrossFade()))
            .into(this)
    }
}

fun ImageView.setVideoCover(uniqueKey: String?, placeholder: Any? = null) {
    val videoResId = R.drawable.ic_dandanplay

    val bitmapRequestBuilder = Glide.with(this).asBitmap()

    val coverFile = uniqueKey.toCoverFile()
    val requestBuilder = when {
        coverFile.isValid() -> {
            bitmapRequestBuilder.load(coverFile!!.absolutePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
        }
        placeholder != null -> {
            bitmapRequestBuilder.load(placeholder)
        }
        else -> {
            setImageResource(videoResId)
            return
        }
    }

    requestBuilder
        .error(videoResId)
        .transition((BitmapTransitionOptions.withCrossFade()))
        .transform(CenterCrop(), RoundedCorners(dp2px(5)))
        .into(this)
}