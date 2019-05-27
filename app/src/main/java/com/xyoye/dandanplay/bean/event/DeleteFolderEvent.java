package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2018/10/25.
 */

public class DeleteFolderEvent {
    private String folderPath;
    private int position;

    public DeleteFolderEvent(String folderPath, int position) {
        this.folderPath = folderPath;
        this.position = position;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
