package com.xyoye.dandanplay.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class SaveCurrentEvent implements Serializable {
    private String folderPath;
    private String videoName;
    private int currentPosition;

    public SaveCurrentEvent() {
    }

    public SaveCurrentEvent(String folderPath, String videoName, int currentPosition) {
        this.folderPath = folderPath;
        this.videoName = videoName;
        this.currentPosition = currentPosition;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
