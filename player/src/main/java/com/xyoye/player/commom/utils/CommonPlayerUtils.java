package com.xyoye.player.commom.utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2018/7/1.
 */

public final class CommonPlayerUtils {

    /**
     * 根据视频文件获取同文件夹下字幕
     * 视频名：test.mp4
     * 字幕名：test.ass | test.sc.ass
     * 过滤：test1.ass
     */
    public static String getSubtitlePath(String videoPath) {
        if (TextUtils.isEmpty(videoPath) || !videoPath.contains("."))
            return "";

        File videoFile = new File(videoPath);
        if (!videoFile.exists())
            return "";

        //可加载的字幕格式
        List<String> extensionList = new ArrayList<>();
        extensionList.add("ASS");
        extensionList.add("SCC");
        extensionList.add("SRT");
        extensionList.add("STL");
        extensionList.add("TTML");

        //无后缀文件路径
        String videoPathNoExt = videoFile.getAbsolutePath();
        int pointIndex = videoPathNoExt.lastIndexOf(".");
        videoPathNoExt = videoPathNoExt.substring(0, pointIndex);

        List<String> subtitlePathList = new ArrayList<>();
        File folderFile = videoFile.getParentFile();
        for (File childFile : folderFile.listFiles()) {
            String childFilePath = childFile.getAbsolutePath();
            //文件路径头与视频路径头相同
            if (childFilePath.startsWith(videoPathNoExt)) {
                String extension = FileUtils.getFileExtension(childFilePath);
                //文件结尾存在与可用字幕格式中
                if (extensionList.contains(extension.toUpperCase())) {
                    //存在xxx.ass直接返回
                    if (childFilePath.length() == videoPathNoExt.length() + extension.length() + 1)
                        return childFilePath;
                    subtitlePathList.add(childFilePath);
                }
            }
        }
        if (subtitlePathList.size() < 1) {
            return "";
        } else if (subtitlePathList.size() == 1) {
            return subtitlePathList.get(0);
        } else {
            for (String subtitlePath : subtitlePathList) {
                String extension = FileUtils.getFileExtension(subtitlePath);
                String centerContent = subtitlePath.substring(videoPathNoExt.length(), subtitlePath.length() - extension.length() - 1);
                //与必须包含“.”，如“.sc”
                if (centerContent.contains("."))
                    return subtitlePath;
            }
        }
        return "";
    }


    @ColorInt
    public static int getResColor(Context context, @ColorRes int colorId){
        return ContextCompat.getColor(context, colorId);
    }
}
