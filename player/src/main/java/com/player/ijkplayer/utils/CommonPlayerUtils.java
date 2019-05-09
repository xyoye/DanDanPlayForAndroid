package com.player.ijkplayer.utils;

import android.annotation.SuppressLint;
import android.widget.Toast;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by long on 2016/10/18.
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
        String[] extArray = new String[]{"ASS", "SCC", "SRT", "STL", "TTML"};
        String videoFolder = FileUtils.getDirName(videoPath);
        String videoName = FileUtils.getFileNameNoExtension(videoPath);

        //遍历父文件夹
        File folderFile = new File(videoFolder);
        for (File file : folderFile.listFiles()){
            String path = file.getAbsolutePath();
            //以视频名称开头
            if (path.startsWith(videoName)){
                for (String ext : extArray){
                    //已可用字幕格式结尾，文件大小大于0
                    if (path.endsWith(ext) && file.length() > 0){
                        return path;
                    }
                }
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
