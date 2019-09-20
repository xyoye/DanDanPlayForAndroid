package com.xyoye.dandanplay.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.xyoye.dandanplay.ui.weight.RoundImageView;
import com.youth.banner.loader.ImageLoader;

/**
 * Created by xyoye on 2018/7/15.
 */

public class GlideImageLoader extends ImageLoader {

    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        Glide.with(context)
                .load(path)
                .transition((DrawableTransitionOptions.withCrossFade()))
                .into(imageView);
    }

    @Override
    public ImageView createImageView(Context context) {
        return new RoundImageView(context);
    }
}
