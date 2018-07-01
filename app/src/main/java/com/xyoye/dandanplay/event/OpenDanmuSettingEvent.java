package com.xyoye.dandanplay.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/1.
 */


public class OpenDanmuSettingEvent implements Serializable {
    private String videoPath;
    private int videoPosition;

    public OpenDanmuSettingEvent(String videoPath, int videoPosition) {
        this.videoPath = videoPath;
        this.videoPosition = videoPosition;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public int getVideoPosition() {
        return videoPosition;
    }

    public void setVideoPosition(int videoPosition) {
        this.videoPosition = videoPosition;
    }
}
