package com.xyoye.player.commom.utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import java.io.File;

/**
 * Created by xyoye on 2018/7/1.
 */

public final class CommonPlayerUtils {

    /**
     * 解析视频同名字幕
     */
    public static String getSubtitlePath(String videoPath){
        if (videoPath == null || "".equals(videoPath)){
            return "";
        }
        File videoFile = new File(videoPath);
        if (!videoFile.exists())
            return "";

        //可加载的字幕格式
        String[] extArray = new String[]{".ass", ".scc", ".srt", ".stl", ".ttml"};
        int lastPoi = videoPath.lastIndexOf(".");
        String videoPathNotExt = videoPath.substring(0, lastPoi);

        for (String ext : extArray){
            File tempFile = new File(videoPathNotExt + ext);
            if (tempFile.exists() && tempFile.length() > 0){
                return tempFile.getAbsolutePath();
            }
        }

        return "";
    }

    @ColorInt
    public static int getResColor(Context context, @ColorRes int colorId){
        return ContextCompat.getColor(context, colorId);
    }
}
