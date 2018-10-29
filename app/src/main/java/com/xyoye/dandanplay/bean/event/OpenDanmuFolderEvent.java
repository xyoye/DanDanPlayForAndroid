package com.xyoye.dandanplay.bean.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class OpenDanmuFolderEvent implements Serializable {
    private String path;
    private int episodeId;
    private boolean isFolder;

    public OpenDanmuFolderEvent(String path,int episodeId, boolean isFolder) {
        this.path = path;
        this.episodeId = episodeId;
        this.isFolder = isFolder;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }
}
