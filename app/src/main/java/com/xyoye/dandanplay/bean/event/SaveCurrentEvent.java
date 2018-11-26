package com.xyoye.dandanplay.bean.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class SaveCurrentEvent implements Serializable {
    private String folderPath;
    private String videoPath;
    private int currentPosition;

    public SaveCurrentEvent() {
    }

    public SaveCurrentEvent(String folderPath, String videoPath, int currentPosition) {
        this.folderPath = folderPath;
        this.videoPath = videoPath;
        this.currentPosition = currentPosition;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
