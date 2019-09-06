package com.xyoye.dandanplay.utils.jlibtorrent;

import com.blankj.utilcode.util.SPUtils;

/**
 * Created by xyoye on 2019/8/23.
 */

public class TorrentConfig {

    public static class Engine {
        public static final String LIB_TORRENT = "lib_torrent";
        public static final String THUNDER = "thunder";
    }


    //引擎类别
    private static final String DOWNLOAD_ENGINE = "download_engine";
    //最大活动任务数量
    private static final String MAX_ACTIVITY_TASK = "max_activity_task";
    //最大下载速度
    private static final String MAX_DOWNLOAD_RATE = "max_download_rate";
    //最大下上传速度
    private static final String MAX_UPLOAD_RATE = "max_upload_rate";

    //仅wifi下下载
    private static final String DOWNLOAD_ONLY_WIFI = "download_only_wifi";

    private static class Holder {
        private static TorrentConfig appConfig = new TorrentConfig();
    }

    private TorrentConfig() {

    }

    public static TorrentConfig getInstance() {
        return Holder.appConfig;
    }

    /**
     * 下载引擎
     */
    public String getDownloadEngine() {
        return SPUtils.getInstance().getString(DOWNLOAD_ENGINE, Engine.LIB_TORRENT);
    }

    public void setDownloadEngine(String engine) {
        SPUtils.getInstance().put(DOWNLOAD_ENGINE, engine);
    }

    /**
     * 最大同时活动任务数量
     */
    public int getMaxTaskCount() {
        return SPUtils.getInstance().getInt(MAX_ACTIVITY_TASK, 4);
    }

    public void setMaxTaskCount(int taskCount) {
        SPUtils.getInstance().put(MAX_ACTIVITY_TASK, taskCount);
    }

    /**
     * 最大下载速度
     */
    public int getMaxDownloadRate() {
        return SPUtils.getInstance().getInt(MAX_DOWNLOAD_RATE, 1000);
    }

    public void setMaxDownloadRate(int rate) {
        SPUtils.getInstance().put(MAX_DOWNLOAD_RATE, rate);
    }

    /**
     * 最大上传速度
     */
    public int getMaxUploadRate() {
        return SPUtils.getInstance().getInt(MAX_UPLOAD_RATE, 1000);
    }

    public void setMaxUploadRate(int rate) {
        SPUtils.getInstance().put(MAX_UPLOAD_RATE, rate);
    }

    /**
     * 仅允许wifi下载
     */
    public boolean isDownloadOnlyWifi() {
        return SPUtils.getInstance().getBoolean(DOWNLOAD_ONLY_WIFI, false);
    }

    public void setDownloadOnlyWifi(boolean isOpen) {
        SPUtils.getInstance().put(DOWNLOAD_ONLY_WIFI, isOpen);
    }
}
