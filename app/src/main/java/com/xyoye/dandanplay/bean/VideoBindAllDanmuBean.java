package com.xyoye.dandanplay.bean;

/**
 * Created by XYJ on 2020/2/15.
 */

public class VideoBindAllDanmuBean {
    private String danmuPath;
    private String videoPath;

    public VideoBindAllDanmuBean(String danmuPath, String videoPath) {
        this.danmuPath = danmuPath;
        this.videoPath = videoPath;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
