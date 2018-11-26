package com.xyoye.dandanplay.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by xyy on 2018-03-03 下午 6:05
 */

public class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
    @SuppressLint("StaticFieldLeak")
    private View view;
    private String imageUrl;
    private LruCache<String, Bitmap> mImageCache;

    public ImageLoadTask(View view) {
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
        } else {
            bitmap = mImageCache.get(imageUrl);
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ImageView iv = view.findViewWithTag(imageUrl);
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
            if (bitmap != null) {
                bitmap = compressBitmap(bitmap);
            } else {
                fmmr.setDataSource(imageUrl);
                bitmap = fmmr.getFrameAtTime();
                Bitmap b2 = fmmr.getFrameAtTime(
                        400,
                        FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (b2 != null) {
                    bitmap = b2;
                }
                bitmap = compressBitmap(bitmap);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            fmmr.release();
        }
        return bitmap;
    }

    //压缩至100k, 640x480
    private static Bitmap compressBitmap(Bitmap bitmap) {
        // 最大图片大小 150k
        int maxSize = 100;
        // 根据设定的最大分辨率获取压缩比例
        int ratio = getRatioSize(bitmap.getWidth(), bitmap.getHeight());

        int afterWidth = bitmap.getWidth() / ratio;
        int afterHeight = bitmap.getHeight() / ratio;

        // 根据比例压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(afterWidth, afterHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), 10, 10, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect rect = new Rect(0, 0, afterWidth, afterHeight);
        canvas.drawBitmap(bitmap, rect, rect, paint);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > maxSize) {
            // 重置baos
            baos.reset();
            options -= 10;
            result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }

        // 保存图片 true表示使用哈夫曼算法
        return result;
    }

    private static int getRatioSize(int bitWidth, int bitHeight) {
        // 图片最大分辨率
        int imageHeight = 640;
        int imageWidth = 480;
        // 缩放比
        int ratio = 1;
        // 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        if (bitWidth > bitHeight && bitWidth > imageWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = bitWidth / imageWidth;
        } else if (bitWidth < bitHeight && bitHeight > imageHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = bitHeight / imageHeight;
        }
        // 最小比率为1
        if (ratio <= 0)
            ratio = 1;
        return ratio;
    }
}