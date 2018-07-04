package com.xyoye.dandanplay.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class OpenDanmuFolderEvent implements Serializable {
    private String path;
    private boolean isFolder;

    public OpenDanmuFolderEvent(String path, boolean isFolder) {
        this.path = path;
        this.isFolder = isFolder;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }
}
