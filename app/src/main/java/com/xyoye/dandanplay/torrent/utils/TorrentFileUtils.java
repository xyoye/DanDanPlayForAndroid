package com.xyoye.dandanplay.torrent.utils;

import android.os.Environment;

import com.xyoye.dandanplay.app.IApplication;

import java.io.File;

/**
 * Created by xyoye on 2019/8/23.
 */

public class TorrentFileUtils {
    //默认应用文件夹
    public static final String DanDanPlayPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DanDanPlay";
    //用户选择缓存目录
    public static final String UserDownloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DanDanPlay";
    //Session配置文件
    public static final String DefaultSessionFilePath = TorrentFileUtils.DanDanPlayPath + "/_config/.session";
    //恢复任务文件
    public static final String TaskResumeFilePath = TorrentFileUtils.DanDanPlayPath + "/_config/.resume";

    /**
     * 获取应用缓存目录
     *
     * @return
     * 获取应用缓存目录：storage/emulated/0/Android/data/com.xyoye.dandanplay
     * 文件夹不存在或不能读写：storage/emulated/0/DanDanPlay
     */
    public static String getSystemCacheDirPath(){
        File cacheDirFile = IApplication.get_context().getExternalCacheDir();
        if (cacheDirFile != null){
            if (cacheDirFile.exists() && cacheDirFile.canRead() && cacheDirFile.canWrite()){
                return cacheDirFile.getAbsolutePath();
            }
        }
        return DanDanPlayPath;
    }

    /**
     * 目录是否可读
     */
    public static boolean isStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
