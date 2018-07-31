package com.xyoye.dandanplay.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by xyy on 2018-03-03 下午 6:05
 */

public class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
    @SuppressLint("StaticFieldLeak")
    private View view;
    private String imageUrl;
    private LruCache<String, Bitmap> mImageCache;

    public ImageLoadTask(View view){
        this.view = view;
        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        imageUrl = params[0];
        Bitmap bitmap;
        if (mImageCache.get(imageUrl) == null) {
            bitmap = downloadImage();
            if (bitmap != null && imageUrl != null)
                mImageCache.put(imageUrl, bitmap);
        }else {
            bitmap = downloadImage();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ImageView iv = (ImageView) view.findViewWithTag(imageUrl);
        if (iv != null && result != null) {
            iv.setImageBitmap(result);
        }
    }

    /**
     * 根据路径获取视频帧
     */
    private Bitmap downloadImage() {
        Bitmap bitmap = null;
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(imageUrl);
            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (bitmap != null){
                if (bitmap.getWidth() > 640) {// 如果图片宽度规格超过640px,则进行压缩
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                            640, 480,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }
            }
            if (bitmap == null) {
                fmmr.setDataSource(imageUrl);
                bitmap = fmmr.getFrameAtTime();
                Bitmap b2 = fmmr.getFrameAtTime(
                        4000000,
                        FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (b2 != null) {
                    bitmap = b2;
                }
                if (bitmap.getWidth() > 640) {// 如果图片宽度规格超过640px,则进行压缩
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                            640, 480,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            fmmr.release();
        }
        return bitmap;
    }
}
