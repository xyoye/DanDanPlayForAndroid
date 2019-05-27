package com.xyoye.dandanplay.bean;

import java.io.Serializable;

/**
 * Created by xyoye on 2018/6/30.
 */


public class VideoBean implements Serializable {
    private int _id;
    private String videoPath;
    private long videoDuration;
    private long videoSize;
    private String danmuPath;
    private long currentPosition;
    private int episodeId;
    private boolean notCover;

    public VideoBean() {
    }

    public VideoBean(String videoPath, boolean notCover, long videoDuration, String danmuPath, long currentPosition, int episodeId) {
        this.videoPath = videoPath;
        this.notCover = notCover;
        this.videoDuration = videoDuration;
        this.danmuPath = danmuPath;
        this.currentPosition = currentPosition;
        this.episodeId = episodeId;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public boolean isNotCover() {
        return notCover;
    }

    public void setNotCover(boolean notCover) {
        this.notCover = notCover;
    }

    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public long getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(long videoSize) {
        this.videoSize = videoSize;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }
}
