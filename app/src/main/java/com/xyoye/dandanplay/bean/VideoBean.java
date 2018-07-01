package com.xyoye.dandanplay.bean;

import java.io.Serializable;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class VideoBean implements Serializable {
    private String videoName;
    private String videoPath;
    private String videoCover;
    private String videoDuration;
    private String danmuPath;
    private String lastProgress;

    public VideoBean() {
    }

    public VideoBean(String videoName, String videoPath) {
        this.videoName = videoName;
        this.videoPath = videoPath;
    }

    public VideoBean(String videoName, String videoPath, String videoCover, String videoDuration, String danmuPath, String lastProgress) {
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.videoCover = videoCover;
        this.videoDuration = videoDuration;
        this.danmuPath = danmuPath;
        this.lastProgress = lastProgress;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public String getLastProgress() {
        return lastProgress;
    }

    public void setLastProgress(String lastProgress) {
        this.lastProgress = lastProgress;
    }
}
