package com.player.commom.utils;

import android.annotation.SuppressLint;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xyoye on 2018/7/1.
 */

public final class CommonPlayerUtils {

    private CommonPlayerUtils() {
        throw new AssertionError();
    }


    /**
     * 时长格式化显示
     */
    @SuppressLint("DefaultLocale")
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60;
//        int minutes = (totalSeconds / 60) % 60;
//        int hours = totalSeconds / 3600;
        return minutes > 99 ? String.format("%d:%02d", minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 下载速度格式化显示
     */
    public static String getFormatSize(int size) {
        long fileSize = (long) size;
        String showSize = "";
        if (fileSize >= 0 && fileSize < 1024) {
            showSize = fileSize + "Kb/s";
        } else if (fileSize >= 1024 && fileSize < (1024 * 1024)) {
            showSize = Long.toString(fileSize / 1024) + "KB/s";
        } else if (fileSize >= (1024 * 1024) && fileSize < (1024 * 1024 * 1024)) {
            showSize = Long.toString(fileSize / (1024 * 1024)) + "MB/s";
        }
        return showSize;
    }

    /**
     * 获取格式化当前时间
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCurFormatTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(System.currentTimeMillis()));
    }


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

    /**
     * 根据进度获取具体对应的大小
     */
    public static float progress2real(int progress){
        return progress2real(progress, 18);
    }

    /**
     * 根据进度获取具体对应的大小
     */
    public static float progress2real(int progress, int dpValue){
        return  (float) progress / 100 * ConvertUtils.dp2px(dpValue);
    }
}
