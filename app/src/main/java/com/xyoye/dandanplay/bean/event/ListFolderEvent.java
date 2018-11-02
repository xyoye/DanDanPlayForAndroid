package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyy on 2018/11/1.
 */

public class ListFolderEvent {
    private String path;

    public ListFolderEvent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
