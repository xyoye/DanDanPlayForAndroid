package com.xyoye.common_component.extension

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.xyoye.common_component.utils.dp2px

/**
 * Created by xyoye on 2020/7/31.
 */

fun ImageView.setGlideImage(
    source: String?,
    dpRadius: Int = 0,
    @DrawableRes errorRes: Int = 0,
    isCache: Boolean = true
) {
    val strategy = if (isCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE

    if (dpRadius > 0) {
        Glide.with(this)
            .asBitmap()
            .error(errorRes)
            .load(source)
            .diskCacheStrategy(strategy)
            .skipMemoryCache(isCache.not())
            .transition((BitmapTransitionOptions.withCrossFade()))
            .transform(CenterCrop(), RoundedCorners(dp2px(dpRadius)))
            .into(this)
    } else {
        Glide.with(this)
            .load(source)
            .error(errorRes)
            .diskCacheStrategy(strategy)
            .skipMemoryCache(isCache.not())
            .transform(CenterCrop())
            .transition((DrawableTransitionOptions.withCrossFade()))
            .into(this)
    }
}

fun ImageView.setGlideImage(source: Uri?, dpRadius: Int = 0, @DrawableRes errorRes: Int = 0) {
    if (dpRadius > 0) {
        Glide.with(this)
            .asBitmap()
            .load(source)
            .error(errorRes)
            .transform(CenterCrop(), RoundedCorners(dp2px(dpRadius)))
            .transition((BitmapTransitionOptions.withCrossFade()))
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