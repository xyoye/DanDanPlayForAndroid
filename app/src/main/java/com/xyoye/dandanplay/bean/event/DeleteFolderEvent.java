package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyy on 2018/10/25.
 */

public class DeleteFolderEvent {
    private String folderPath;

    public DeleteFolderEvent(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
