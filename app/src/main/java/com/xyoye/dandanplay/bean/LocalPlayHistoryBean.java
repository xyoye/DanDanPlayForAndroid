package com.xyoye.dandanplay.bean;

/**
 * Created by xyoye on 2019/9/2.
 */

public class LocalPlayHistoryBean {
    private String videoPath;
    private String videoTitle;
    private String danmuPath;
    private int episodeId;
    private int sourceOrigin;
    private long playTime;

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public int getSourceOrigin() {
        return sourceOrigin;
    }

    public void setSourceOrigin(int sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
}
