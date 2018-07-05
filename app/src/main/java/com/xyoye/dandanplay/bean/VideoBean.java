package com.xyoye.dandanplay.bean;

import java.io.Serializable;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class VideoBean implements Serializable {
    private String videoName;
    private String videoPath;
    private String videoCover;
    private long videoDuration;
    private String danmuPath;
    private int currentPosition;

    public VideoBean() {
    }

    public VideoBean(String videoName, String videoPath, String danmuPath, int currentPosition, long videoDuration) {
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.danmuPath = danmuPath;
        this.currentPosition = currentPosition;
        this.videoDuration = videoDuration;
    }

    public VideoBean(String videoName, String videoPath, String videoCover, long videoDuration, String danmuPath, int currentPosition) {
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.videoCover = videoCover;
        this.videoDuration = videoDuration;
        this.danmuPath = danmuPath;
        this.currentPosition = currentPosition;
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

    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
